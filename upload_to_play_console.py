#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Upload IrishVisaExpectedDate v1.10.5 AAB to Play Console via Google Play API.
Uses service account authentication.
"""

import sys
import os
sys.stdout.reconfigure(encoding='utf-8') if hasattr(sys.stdout, 'reconfigure') else None
from google.oauth2.service_account import Credentials
from googleapiclient.discovery import build
from googleapiclient.http import MediaFileUpload

# Configuration
PACKAGE_NAME = "com.shayshankrathore.irishvisadate"
VERSION_CODE = 31
VERSION_NAME = "1.10.5"
SERVICE_ACCOUNT_FILE = "play-service-account.json"
AAB_FILE = r"app\build\outputs\bundle\release\app-release.aab"
TRACK = "internal"  # internal, alpha, beta, or production

def get_credentials():
    """Authenticate using service account."""
    scopes = ["https://www.googleapis.com/auth/androidpublisher"]
    credentials = Credentials.from_service_account_file(
        SERVICE_ACCOUNT_FILE, scopes=scopes
    )
    return credentials

def upload_aab():
    """Upload AAB and create release."""
    try:
        # Check if AAB file exists
        if not os.path.exists(AAB_FILE):
            print(f"✗ AAB file not found: {AAB_FILE}")
            return False

        print(f"✓ Found AAB file: {AAB_FILE}")
        print(f"  Size: {os.path.getsize(AAB_FILE) / (1024*1024):.1f} MB")

        credentials = get_credentials()
        service = build("androidpublisher", "v3", credentials=credentials)

        # Create edit session
        edit = service.edits().insert(body={}, packageName=PACKAGE_NAME).execute()
        edit_id = edit["id"]
        print(f"✓ Created edit session: {edit_id}")

        # Upload AAB
        print(f"✓ Uploading AAB to Play Console...")
        aab_upload = MediaFileUpload(AAB_FILE, mimetype="application/octet-stream", resumable=True)

        bundle_response = service.edits().bundles().upload(
            packageName=PACKAGE_NAME,
            editId=edit_id,
            media_body=aab_upload
        ).execute()

        print(f"✓ AAB uploaded successfully (versionCode: {bundle_response['versionCode']})")

        # Create release in internal track
        print(f"✓ Creating release in {TRACK} track...")
        release_body = {
            "releases": [
                {
                    "versionCodes": [VERSION_CODE],
                    "status": "completed",
                    "releaseNotes": [
                        {
                            "language": "en-US",
                            # TODO: no source changes were found since v1.10.4 (versionCode 30) as of this
                            # edit. Replace this placeholder with real release notes before uploading.
                            "text": "v1.10.5 - TODO: describe what changed in this release"
                        }
                    ]
                }
            ]
        }

        service.edits().tracks().update(
            packageName=PACKAGE_NAME,
            editId=edit_id,
            track=TRACK,
            body=release_body
        ).execute()
        print(f"✓ Release created in {TRACK} track")

        # Commit changes
        service.edits().commit(
            packageName=PACKAGE_NAME, editId=edit_id
        ).execute()
        print(f"✓ Changes committed to Play Console")

        print("\n" + "=" * 60)
        print(f"✓ Successfully uploaded v{VERSION_NAME} to {TRACK} track!")
        print(f"  versionCode: {VERSION_CODE}")
        print(f"  Next steps:")
        print(f"  1. Review in Play Console")
        print(f"  2. Run: python promote_to_production.py")
        print(f"     to move to production track (hotfix)")
        print("=" * 60)
        return True

    except Exception as e:
        print(f"✗ Error: {e}")
        import traceback
        traceback.print_exc()
        return False

if __name__ == "__main__":
    print("=" * 60)
    print(f"Uploading IrishVisaExpectedDate v{VERSION_NAME} to Play Console")
    print("=" * 60)
    success = upload_aab()
    sys.exit(0 if success else 1)
