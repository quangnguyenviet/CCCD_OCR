import argparse
import json
import time

def main():
    parser = argparse.ArgumentParser(description="Extract info from CCCD image")
    parser.add_argument("--image", required=True, help="Path to input image")
    parser.add_argument("--card-model", default="", help="Path to card model")
    parser.add_argument("--roi-model", default="", help="Path to roi model")
    parser.add_argument("--ocr-model", default="", help="Path to ocr model")
    
    args = parser.parse_args()

    # Giả lập thời gian load AI models và xử lý (sleep 1 giay)
    time.sleep(1)

    # Output dữ liệu fix cứng như yêu cầu của User
    result = {
        "data": {
            "cccd_number": "001201012345",
            "full_name": "NGUYEN VAN A",
            "date_of_birth": "01/01/2000",
            "gender": "Nam",
            "nationality": "Việt Nam",
            "place_of_origin": "Hà Nội",
            "place_of_residence": "Quận Ba Đình, Hà Nội"
        }
    }
    
    # Print kết quả dạng JSON ra Stdout để backend Java đón lấy
    print(json.dumps(result, ensure_ascii=False))

if __name__ == "__main__":
    main()
