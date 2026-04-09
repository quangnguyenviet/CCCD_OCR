import argparse
import os
import tempfile
import zipfile
import traceback
from pathlib import Path

import yaml


def find_data_yaml(root: Path) -> Path:
    candidates = list(root.rglob("data.yaml"))
    if not candidates:
        raise FileNotFoundError("Không tìm thấy data.yaml trong dataset ZIP")
    return candidates[0]


def find_images_dir(root: Path) -> Path:
    preferred = root / "images"
    if preferred.exists() and preferred.is_dir():
        return preferred

    candidates = [path for path in root.rglob("images") if path.is_dir()]
    if not candidates:
        raise FileNotFoundError("Không tìm thấy thư mục images trong dataset ZIP")
    return candidates[0]


def find_labels_dir(root: Path) -> Path:
    preferred = root / "labels"
    if preferred.exists() and preferred.is_dir():
        return preferred

    candidates = [path for path in root.rglob("labels") if path.is_dir()]
    if not candidates:
        raise FileNotFoundError("Không tìm thấy thư mục labels trong dataset ZIP")
    return candidates[0]


def main() -> int:
    try:
        parser = argparse.ArgumentParser(description="Train YOLO model from dataset ZIP")
        parser.add_argument("--zip", dest="zip_path", required=True, help="Path to dataset zip")
        parser.add_argument("--epochs", type=int, default=10)
        parser.add_argument("--batch", type=int, default=8)
        parser.add_argument("--imgsz", type=int, default=640)
        parser.add_argument("--weights", default="yolov8n.pt")
        args = parser.parse_args()

        print("PROGRESS=15", flush=True)

        from ultralytics import YOLO

        zip_path = Path(args.zip_path).resolve()
        if not zip_path.exists():
            raise FileNotFoundError(f"ZIP không tồn tại: {zip_path}")

        with tempfile.TemporaryDirectory(prefix="yolo_dataset_") as tmp_dir:
            extract_dir = Path(tmp_dir)
            with zipfile.ZipFile(zip_path, "r") as zf:
                zf.extractall(extract_dir)

            print("PROGRESS=30", flush=True)
            data_yaml = find_data_yaml(extract_dir)
            print(f"[INFO] data.yaml: {data_yaml}", flush=True)

            images_dir = find_images_dir(extract_dir)
            labels_dir = find_labels_dir(extract_dir)
            print(f"[INFO] images dir: {images_dir}", flush=True)
            print(f"[INFO] labels dir: {labels_dir}", flush=True)

            raw_yaml = yaml.safe_load(data_yaml.read_text(encoding="utf-8")) or {}
            names = raw_yaml.get("names")
            if isinstance(names, dict):
                names = [names[key] for key in sorted(names)]
            if not isinstance(names, list) or not names:
                raise ValueError("data.yaml không có danh sách class names hợp lệ")

            image_files = [p for p in images_dir.rglob("*") if p.is_file() and p.suffix.lower() in {".jpg", ".jpeg", ".png", ".bmp", ".webp"}]
            label_files = [p for p in labels_dir.rglob("*") if p.is_file() and p.suffix.lower() == ".txt"]
            print(f"[INFO] image count: {len(image_files)}", flush=True)
            print(f"[INFO] label count: {len(label_files)}", flush=True)
            if not image_files:
                raise ValueError("Không tìm thấy ảnh nào trong thư mục images")
            if not label_files:
                raise ValueError("Không tìm thấy file nhãn nào trong thư mục labels")

            train_images_dir = (images_dir / "train") if (images_dir / "train").is_dir() else images_dir
            val_images_dir = (images_dir / "val") if (images_dir / "val").is_dir() else train_images_dir
            test_images_dir = (images_dir / "test") if (images_dir / "test").is_dir() else None

            # Build a minimal dataset config using absolute paths only.
            prepared_yaml = {
                "train": train_images_dir.resolve().as_posix(),
                "val": val_images_dir.resolve().as_posix(),
                "nc": len(names),
                "names": names,
            }
            if test_images_dir is not None:
                prepared_yaml["test"] = test_images_dir.resolve().as_posix()

            prepared_data_yaml = extract_dir / "prepared_data.yaml"
            with open(prepared_data_yaml, "w", encoding="utf-8") as f:
                yaml.safe_dump(prepared_yaml, f, allow_unicode=True, sort_keys=False)

            # Also overwrite the extracted original data.yaml so even when
            # Ultralytics internally resolves/rewrites dataset args, it still
            # points to the corrected absolute train/val/test paths.
            with open(data_yaml, "w", encoding="utf-8") as f:
                yaml.safe_dump(prepared_yaml, f, allow_unicode=True, sort_keys=False)

            print(f"[INFO] prepared dataset config: {prepared_yaml}", flush=True)
            print(f"[INFO] prepared dataset yaml: {prepared_data_yaml}", flush=True)
            print(f"[DEBUG] images_dir absolute: {images_dir.resolve()}", flush=True)
            print(f"[DEBUG] train dir absolute: {train_images_dir.resolve()}", flush=True)
            print(f"[DEBUG] val dir absolute: {val_images_dir.resolve()}", flush=True)
            print(f"[DEBUG] sample images: {[str(p) for p in image_files[:5]]}", flush=True)

            model = YOLO(args.weights)

            print("PROGRESS=40", flush=True)
            previous_cwd = Path.cwd()
            os.chdir(extract_dir)
            try:
                result = model.train(
                    data=str(data_yaml),
                    epochs=args.epochs,
                    imgsz=args.imgsz,
                    batch=args.batch,
                    verbose=True,
                )
            finally:
                os.chdir(previous_cwd)

            save_dir = getattr(result, "save_dir", None)
            if save_dir is not None:
                best_path = Path(save_dir) / "weights" / "best.pt"
                if best_path.exists():
                    print(f"MODEL_PATH={best_path.resolve()}", flush=True)

        print("PROGRESS=100", flush=True)
        return 0
    except Exception as exc:
        print(f"[FATAL] {type(exc).__name__}: {exc}", flush=True)
        traceback.print_exc()
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
