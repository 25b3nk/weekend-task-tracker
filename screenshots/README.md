# Screenshots Directory

This directory contains screenshots for the Weekend Task Tracker app documentation.

## Screenshot Guidelines

### Required Screenshots:
1. `main-screen.png` - Main screen showing the three tabs (Weekend, Master List, Completed)
2. `add-task.png` - Add task screen with NLP input
3. `voice-input.png` - Voice input feature demonstration
4. `task-list.png` - Task list with various priorities and due dates
5. `edit-task.png` - Edit task screen
6. `notification.png` - Task reminder notification example

### Optional Screenshots:
- `nlp-parsing.png` - NLP parsing preview in action
- `completed-tasks.png` - Completed tasks tab
- `task-menu.png` - Task options menu (edit, delete, move)

## Image Specifications

- **Format**: PNG (preferred) or JPG
- **Resolution**: 1080x2400 or device resolution
- **Size**: Keep under 500KB per image (use compression if needed)
- **Naming**: Use lowercase with hyphens (e.g., `add-task.png`)

## How to Capture Screenshots

### Using Android Studio:
1. Run the app on an emulator or device
2. Click the camera icon in the device toolbar
3. Save to this directory

### Using ADB:
```bash
# Take screenshot
adb shell screencap -p /sdcard/screenshot.png

# Pull to computer
adb pull /sdcard/screenshot.png screenshots/main-screen.png
```

### On Device:
- Press Power + Volume Down
- Transfer via USB or Google Photos

## Adding to README

Screenshots are referenced in the main README using:
```markdown
![Description](screenshots/filename.png)
```
