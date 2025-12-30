# System Security Monitor Application

This tool provides an easy and secure method for encrypting, decrypting, and scanning files on Windows-based systems. It uses AES-256-GCM encryption, PBKDF2 for key derivation, and a ZIP scanner to analyze compressed archives. The tool is lightweight, portable, and does not require installation, making it ideal for users who need reliable file encryption and scanning.

## Features

Encrypt and Decrypt Files: Uses AES-256-GCM for strong encryption with PBKDF2 key derivation.

ZIP Archive Scanning: Scans ZIP files for suspicious file types or dangerous patterns.

Log Files: Automatically generates logs for every action, detailing timestamps, errors, and other useful information.

No Installation Required: Simply download and run the .exe file. No need for installation or external dependencies.

Offline Use: Completely offline; does not rely on external services.

Minimal System Requirements: Works on Windows 10 and above with as little as 4GB of RAM.

## Installation

Download the .exe file from the release section.

Place the file in any directory where you want to store your encrypted or scanned files.

Optionally, create a shortcut on your desktop for easier access.

Run the tool by double-clicking the .exe file. If Windows SmartScreen warns about the file being from an unknown publisher, click "More Info" and then "Run Anyway".

#System Requirements

Windows 10 or above.

At least 4GB of RAM.

No additional software or installation needed.

## Usage
### Encrypting Files

Select a file or folder to encrypt.

Provide a strong password or passphrase.

The tool will encrypt the file and save it with a .enc extension.

A log entry will confirm the encryption status.

#Decrypting Files

Click on the .enc file.

Enter the original password.

If the password is correct, the file will be decrypted and saved in the same directory.

#Scanning ZIP Files

Select a ZIP archive to scan.

The tool will identify suspicious file types or hidden scripts and generate a log report.

### Logs

Logs will be saved in the same directory as the application. These contain timestamps, actions performed, and any issues detected during operations.

## Design and Architecture

The tool is built on a modular architecture, consisting of the following key components:

Encryption Module: Handles file encryption using AES-256-GCM.

Key Management Module: Derives cryptographic keys using PBKDF2 and securely stores them.

ZIP Scanner Module: Scans ZIP files for potentially dangerous content.

Logging System: Automatically generates log files with detailed information.

Java was chosen for its portability, security libraries, and the availability of standard tools for encryption and file processing. The system is designed to be modular, allowing for easy future updates and extensions.

## Security Considerations

The tool avoids writing temporary unencrypted data on the system and ensures sensitive buffers are cleared.

Log files do not contain sensitive information, preventing accidental leakage.

The security of the system largely depends on the strength of the user's password. A strong password is recommended for maximum protection.
