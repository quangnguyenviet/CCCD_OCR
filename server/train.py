import argparse
import tempfile
import zipfile
from pathlib import Path


def find_data_yaml(root: Path) -> Path:
    candidates = list(root.rglob("data.yaml"))
    if not candidates:
        raise FileNotFoundError("Không tìm thấy data.yaml trong dataset ZIP")
    return candidates[0]


def main() -> int:
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

        model = YOLO(args.weights)

        print("PROGRESS=40", flush=True)
        result = model.train(
            data=str(data_yaml),
            epochs=args.epochs,
            imgsz=args.imgsz,
            batch=args.batch,
            verbose=True,
        )

        save_dir = getattr(result, "save_dir", None)
        if save_dir is not None:
            best_path = Path(save_dir) / "weights" / "best.pt"
            if best_path.exists():
                print(f"MODEL_PATH={best_path.resolve()}", flush=True)

    print("PROGRESS=100", flush=True)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
