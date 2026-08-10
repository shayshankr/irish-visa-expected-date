#!/usr/bin/env python3
"""
Promote IrishVisaExpectedDate v1.10.2 from alpha to production track.
Uses Google Play API via service account authentication.
"""

import json
import sys
from google.auth.transport.requests import Request
from google.oauth2.service_account import Credentials
from googleapiclient.discovery import build

# Configuration
PACKAGE_NAME = "com.shayshankrathore.irishvisadate"
VERSION_CODE = 28
SERVICE_ACCOUNT_FILE = "play-service-account.json"

def get_credentials():
    """Authenticate using service account."""
    scopes = ["https://www.googleapis.com/auth/androidpublisher"]
    credentials = Credentials.from_service_account_file(
        SERVICE_ACCOUNT_FILE, scopes=scopes
    )
    return credentials

def promote_release():
    """Promote release from alpha to production."""
    try:
        credentials = get_credentials()
        service = build("androidpublisher", "v3", credentials=credentials)

        # Get the edit for the app
        edit = service.edits().insert(body={}, packageName=PACKAGE_NAME).execute()
        edit_id = edit["id"]
        print(f"✓ Created edit session: {edit_id}")

        # Get current internal release
        releases = service.edits().tracks().get(
            packageName=PACKAGE_NAME, editId=edit_id, track="internal"
        ).execute()

        internal_releases = releases.get("releases", [])
        print(f"✓ Found {len(internal_releases)} release(s) in internal")

        # Find our release
        our_release = None
        for release in internal_releases:
            version_codes = release.get("versionCodes", [])
            if VERSION_CODE in [int(v) if isinstance(v, str) else v for v in version_codes]:
                our_release = release
                break

        if not our_release:
            print(f"✗ Could not find release with versionCode {VERSION_CODE} in alpha")
            return False

        print(f"✓ Found release v1.10.2 in alpha")

        # Promote to production by updating production track
        production_body = {
            "releases": [
                {
                    "versionCodes": [VERSION_CODE],
                    "status": "completed",
                    "releaseNotes": our_release.get("releaseNotes", [])
                }
            ]
        }

        service.edits().tracks().update(
            packageName=PACKAGE_NAME,
            editId=edit_id,
            track="production",
            body=production_body
        ).execute()
        print(f"✓ Moved v1.10.2 to production track")

        # Clear from internal
        internal_body = {"releases": []}

        service.edits().tracks().update(
            packageName=PACKAGE_NAME,
            editId=edit_id,
            track="internal",
            body=internal_body
        ).execute()
        print(f"✓ Removed v1.10.2 from internal track")

        # Commit the changes
        service.edits().commit(
            packageName=PACKAGE_NAME, editId=edit_id
        ).execute()
        print(f"✓ Committed changes to Play Store")

        return True

    except Exception as e:
        print(f"✗ Error: {e}")
        return False

if __name__ == "__main__":
    print("=" * 60)
    print("Promoting IrishVisaExpectedDate v1.10.2 to Production")
    print("=" * 60)
    success = promote_release()
    sys.exit(0 if success else 1)
