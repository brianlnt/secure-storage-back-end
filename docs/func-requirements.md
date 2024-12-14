
# Functional Requirements

## User Account

**Account Creation**
1. Users should be able to create new accounts by providing basic information, a unique email address, and a password.
2. All newly created accounts should remain disabled until they are verified.
3. The system should send a verification email containing a confirmation link to new users.
4. Users should only be allowed to log in after successfully verifying their accounts.

**Login**
1. Users should be able to log in using their email and password.
2. If Multi-Factor Authentication (MFA) is enabled, users should be prompted to provide a QR code after entering valid credentials.
3. Accounts should be locked for 15 minutes after 6 failed login attempts to prevent brute-force attacks.
4. Passwords should expire every 90 days, requiring users to update them before they can log in again.

**Password Reset**
1. Users should be able to reset their passwords.
2. The system should send a password reset link to the user's email, and the link should become invalid once clicked.
3. Upon clicking the link, users should be presented with a form to create a new password.
4. Once the password is successfully reset, users should be able to log in using the new password.
5. Users should be able to reset their password as often as needed.

**Multi-Factor Authentication (MFA)**
1. Users should have the option to enable MFA for added account security.
2. The MFA process should use a QR code scanned via the user's mobile phone.
3. Users should be able to scan the QR code with an authenticator app to set up MFA.
4. To log in with MFA enabled, users should enter the code from their authenticator app.

**Profile Management**
1. Users should be able to update their basic profile information while logged in.
2. Users should be able to change their password while logged in.
3. Users should be able to modify their account settings while logged in.
4. Users should be able to update their profile picture while logged in.

## Document Management

**Document List**
1. Users should see a list of all uploaded documents on the homepage.
2. Each document should display key details such as name, size, owner, and type.
3. Logged-in users should be able to upload new documents.
4. The document list should support pagination.
5. Users should be able to set the number of documents displayed per page.
6. Users should be able to search for documents by name, with the results also supporting pagination.
7. Users should be able to click on a document to view its details.

**Document Details**
1. Clicking on a document should display its details.
2. Document details should include information on the document’s owner.
3. Users should be able to update the document’s name and description on its details page.
4. Users should be able to download the document from its details page.
5. Users should be able to delete the document from its details page.

## Access Control

**User Roles and Permissions**
1. The system should assign roles to users.
2. Each role should have a defined set of permissions (authorities).
3. Roles should grant different levels of access within the system.
4. Only users with the necessary roles should be allowed to perform certain actions.
5. Only users with non-user roles should be able to update account settings.
6. Only users with non-user roles should be able to update account roles.
7. Only users with "delete" permissions should be able to delete documents.
8. Only users with non-user roles should be able to view other users in the system.

## Audit Trail
1. The system should track and record who created each entity (user, document, etc.).
2. The system should track and record when each entity was created.
3. The system should track and record who updated each entity.
4. The system should track and record when each entity was updated.
