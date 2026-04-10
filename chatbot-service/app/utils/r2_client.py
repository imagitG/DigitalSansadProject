import boto3
import logging
from botocore.client import Config
from botocore.exceptions import ClientError
from app.config import (
    R2_ACCESS_KEY,
    R2_SECRET_KEY,
    R2_BUCKET_NAME,
    R2_ENDPOINT
)

logger = logging.getLogger(__name__)


class R2Client:

    def __init__(self):
        try:
            self.client = boto3.client(
                "s3",
                endpoint_url=R2_ENDPOINT,
                aws_access_key_id=R2_ACCESS_KEY,
                aws_secret_access_key=R2_SECRET_KEY,
                config=Config(signature_version="s3v4"),
                region_name="auto"
            )
            self.bucket = R2_BUCKET_NAME
            logger.info(f"✅ R2Client initialized with bucket: {self.bucket}")
        except Exception as e:
            logger.error(f"❌ Failed to initialize R2Client: {str(e)}")
            raise

    # 📄 LIST FILES
    def list_files(self, prefix: str):
        try:
            logger.info(f"📄 Listing files from R2 with prefix: {prefix}")
            response = self.client.list_objects_v2(
                Bucket=self.bucket,
                Prefix=prefix
            )
            contents = response.get("Contents", [])
            pdf_files = [obj["Key"] for obj in contents if obj["Key"].endswith(".pdf")]
            logger.info(f"✅ Found {len(pdf_files)} PDF files in {prefix}")
            return pdf_files
        except ClientError as e:
            logger.error(f"❌ S3 error listing files from {prefix}: {e.response['Error']['Message']}")
            raise
        except Exception as e:
            logger.error(f"❌ Error listing files from {prefix}: {str(e)}")
            raise

    # 📥 DOWNLOAD FILE
    def download_file_bytes(self, key: str) -> bytes:
        try:
            logger.info(f"📥 Downloading file from R2: {key}")
            response = self.client.get_object(
                Bucket=self.bucket,
                Key=key
            )
            file_bytes = response["Body"].read()
            logger.info(f"✅ Successfully downloaded {key} ({len(file_bytes)} bytes)")
            return file_bytes
        except ClientError as e:
            if e.response['Error']['Code'] == 'NoSuchKey':
                logger.warning(f"⚠️ File not found in R2: {key}")
            else:
                logger.error(f"❌ S3 error downloading {key}: {e.response['Error']['Message']}")
            raise
        except Exception as e:
            logger.error(f"❌ Error downloading {key}: {str(e)}")
            raise

    # 📤 UPLOAD FILE
    def upload_file(self, key: str, data: bytes):
        try:
            logger.info(f"📤 Uploading file to R2: {key} ({len(data)} bytes)")
            self.client.put_object(
                Bucket=self.bucket,
                Key=key,
                Body=data
            )
            logger.info(f"✅ Successfully uploaded {key} to R2")
        except ClientError as e:
            logger.error(f"❌ S3 error uploading {key}: {e.response['Error']['Message']}")
            raise
        except Exception as e:
            logger.error(f"❌ Error uploading {key}: {str(e)}")
            raise