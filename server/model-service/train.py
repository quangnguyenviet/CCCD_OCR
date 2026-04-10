import argparse
import csv
import json
from datetime import datetime
from pathlib import Path

from ultralytics import YOLO


def parse_args():
    parser = argparse.ArgumentParser(description='Train CCCD detection model')
    parser.add_argument('--data', required=True, help='Absolute path to data.yaml')
    parser.add_argument('--epochs', type=int, default=10)
    parser.add_argument('--batch-size', type=int, default=16)
    parser.add_argument('--imgsz', type=int, default=640)
    parser.add_argument('--weights', default='yolov8n.pt')
    parser.add_argument('--project', default='cccd_project')
    parser.add_argument('--name', default='yolov8_cccd')
    parser.add_argument('--train-ratio', type=float, default=0.7)
    parser.add_argument('--val-ratio', type=float, default=0.2)
    parser.add_argument('--test-ratio', type=float, default=0.1)
    return parser.parse_args()


def main():
    args = parse_args()
    data_path = Path(args.data).resolve()
    started_at = datetime.now().astimezone()

    print('Training configuration:')
    print(f'  data: {data_path}')
    print(f'  epochs: {args.epochs}')
    print(f'  batch_size: {args.batch_size}')
    print(f'  imgsz: {args.imgsz}')
    print(f'  weights: {args.weights}')
    print(f'  project: {args.project}')
    print(f'  name: {args.name}')
    print(f'  train_ratio: {args.train_ratio}')
    print(f'  val_ratio: {args.val_ratio}')
    print(f'  test_ratio: {args.test_ratio}')

    model = YOLO(args.weights)
    results = model.train(
        data=str(data_path),
        epochs=args.epochs,
        batch=args.batch_size,
        imgsz=args.imgsz,
        project=args.project,
        name=args.name,
    )

    ended_at = datetime.now().astimezone()

    save_dir = Path(getattr(results, 'save_dir', Path(args.project) / args.name)).resolve()
    results_csv = save_dir / 'results.csv'

    final_loss = None
    metrics = {}

    if results_csv.exists():
        with results_csv.open('r', encoding='utf-8', newline='') as f:
            rows = list(csv.DictReader(f))
            if rows:
                last = rows[-1]

                def to_float(key):
                    value = last.get(key)
                    if value is None or value == '':
                        return None
                    try:
                        return float(value)
                    except ValueError:
                        return None

                val_box = to_float('val/box_loss')
                val_cls = to_float('val/cls_loss')
                val_dfl = to_float('val/dfl_loss')
                train_box = to_float('train/box_loss')
                train_cls = to_float('train/cls_loss')
                train_dfl = to_float('train/dfl_loss')

                if any(v is not None for v in [val_box, val_cls, val_dfl]):
                    final_loss = (val_box or 0.0) + (val_cls or 0.0) + (val_dfl or 0.0)
                elif any(v is not None for v in [train_box, train_cls, train_dfl]):
                    final_loss = (train_box or 0.0) + (train_cls or 0.0) + (train_dfl or 0.0)

                precision = to_float('metrics/precision(B)')
                recall = to_float('metrics/recall(B)')
                map50 = to_float('metrics/mAP50(B)')
                map5095 = to_float('metrics/mAP50-95(B)')
                fitness = to_float('fitness')

                if precision is not None:
                    metrics['precision'] = precision
                if recall is not None:
                    metrics['recall'] = recall
                if map50 is not None:
                    metrics['mAP50'] = map50
                if map5095 is not None:
                    metrics['mAP50-95'] = map5095
                if fitness is not None:
                    metrics['fitness'] = fitness

    summary = {
        'training_start_time': started_at.isoformat(),
        'training_end_time': ended_at.isoformat(),
        'duration_seconds': int((ended_at - started_at).total_seconds()),
        'save_dir': str(save_dir),
        'results_csv': str(results_csv),
        'final_loss': final_loss,
        'metrics': metrics,
    }

    print('TRAINING_SUMMARY_JSON:' + json.dumps(summary, ensure_ascii=False))

    print(results)


if __name__ == '__main__':
    main()
