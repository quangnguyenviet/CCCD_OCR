import argparse
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

    print(results)


if __name__ == '__main__':
    main()
