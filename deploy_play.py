"""
Deploy a signed release AAB to Google Play production.

Usage:
    python deploy_play.py
    python deploy_play.py --notes "What changed in this release"

Requirements:
    pip install google-api-python-client google-auth
"""

import argparse
from google.oauth2 import service_account
from googleapiclient.discovery import build
from googleapiclient.http import MediaFileUpload

KEY_FILE  = r"C:\Users\shays\Downloads\play-credentials.json"
PACKAGE   = "com.shayshankrathore.irishvisadate"
AAB_PATH  = r"C:\Users\shays\AndroidStudioProjects\IrishVisaExpectedDate\app\build\outputs\bundle\release\app-release.aab"
TRACK     = "production"

parser = argparse.ArgumentParser()
parser.add_argument("--notes", default="Bug fixes and improvements.", help="Release notes (en-US)")
args = parser.parse_args()

creds = service_account.Credentials.from_service_account_file(
    KEY_FILE, scopes=["https://www.googleapis.com/auth/androidpublisher"])
service = build("androidpublisher", "v3", credentials=creds)
edits   = service.edits()

edit    = edits.insert(packageName=PACKAGE, body={}).execute()
edit_id = edit["id"]
print(f"Edit created: {edit_id}")

media  = MediaFileUpload(AAB_PATH, mimetype="application/octet-stream", resumable=True)
bundle = edits.bundles().upload(
    packageName=PACKAGE, editId=edit_id, media_body=media).execute()
version_code = bundle["versionCode"]
print(f"AAB uploaded — versionCode: {version_code}")

edits.tracks().update(
    packageName=PACKAGE,
    editId=edit_id,
    track=TRACK,
    body={
        "releases": [{
            "versionCodes": [str(version_code)],
            "status": "completed",
            "releaseNotes": [{"language": "en-US", "text": args.notes}],
        }]
    },
).execute()
print(f"Track '{TRACK}' updated.")

edits.commit(packageName=PACKAGE, editId=edit_id).execute()
print(f"Done! versionCode {version_code} is live on {TRACK}.")
