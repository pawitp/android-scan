# Scan for Android
A simple application to scan documents using wireless scanners on Android.

![main_page](https://user-images.githubusercontent.com/25575/173224158-c7d8017e-cf0b-473d-b249-aa45e7501fc7.png)
![crop_page](https://user-images.githubusercontent.com/25575/173224156-af03d008-debe-4aac-b28a-e5dd4e9ccc80.png)

## Features
 - Crop and rotate the scanned image without JPEG recompression
 - Support JPEG (encoded by the scanner) or PNG (lossless)

## Limitations
 - Works only with HP wireless printers/scanners (tested on HP Deskjet Ink Advantage 3545)

## Libraries
This project makes use of the following open source libraries:
 - [Android Image Cropper](https://github.com/ArthurHub/Android-Image-Cropper) - UI for cropping
 - [JpegKit](https://github.com/CameraKit/jpegkit-android) - Losslessly crop and rotate JPEG images
 - [PNGJ](https://github.com/leonbloy/pngj) - Encode PNG images
