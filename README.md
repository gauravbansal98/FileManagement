# File Management API

This Spring Boot application provides a set of APIs for managing files, including uploading, downloading, updating, and deleting files. Users can interact with these APIs to perform various file operations.

## APIs

### 1. Upload File API

Allow users to upload files onto the platform.

- **Endpoint:** `POST /files/upload`
- **Input:**
    - `file` (Multipart Form Data): The file to be uploaded.
    - `metadata` (Optional): Additional metadata about the file.
- **Output:**
    - A unique file identifier.

### 2. Read File API

Retrieve a specific file based on a unique identifier.

- **Endpoint:** `GET /files/{fileId}`
- **Input:**
    - `fileId` (Path Variable): Unique file identifier.
- **Output:**
    - File binary data.

### 3. Update File API

Update an existing file or its metadata.

- **Endpoint:** `PUT /files/{fileId}`
- **Input:**
    - `file` (Multipart Form Data, Optional): The updated file data.
    - `metadata` (Optional): New metadata for the file.
- **Output:**
    - Updated metadata or a success message.

### 4. Delete File API

Delete a specific file based on a unique identifier.

- **Endpoint:** `DELETE /files/{fileId}`
- **Input:**
    - `fileId` (Path Variable): Unique file identifier.
- **Output:**
    - A success or failure message.

### 5. List Files API

List all available files and their metadata.

- **Endpoint:** `GET /files`
- **Input:** None
- **Output:**
    - A list of file metadata objects, including file IDs, names, creation timestamps, and more.

## Test Cases

The following test cases are available to verify the functionality of the APIs:

- **List Files (Success):** Test the successful listing of files.
- **Upload File (Success):** Test the successful upload of a file.
- **View File (Success):** Test the successful retrieval of a file.
- **Delete File (Success):** Test the successful deletion of a file.
- **Update File (Success):** Test the successful update of a file.
- **Upload File (Failure):** Test the failure scenario for uploading a file.
- **View File (Failure):** Test the failure scenario for retrieving a file.
- **Delete File (Failure):** Test the failure scenario for deleting a file.

Each test case covers different scenarios to ensure the reliability and correctness of the APIs.

## Usage

To use this api you can use the postman collection present inside the repo

## Dependencies

This application uses Spring Boot, Spring Web, Spring Data JPA, and H2 (an in-memory database) for file metadata storage. Make sure to check the `pom.xml` file for a complete list of dependencies.

## Setup

1. Clone this repository.
2. Build and run the application.
3. Access the APIs by making HTTP requests to the provided endpoints.
