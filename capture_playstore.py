import subprocess
import time
import os
from PIL import Image, ImageDraw, ImageFont, ImageFilter

ADB = "/home/nikhil/Android/Sdk/platform-tools/adb"
SERIAL = "1411831572065089"
RAW_DIR = "/home/nikhil/Startup/ApnaSandesh/screenshots/playstore/raw"
READY_DIR = "/home/nikhil/Startup/ApnaSandesh/screenshots/playstore/playstore_ready"

os.makedirs(RAW_DIR, exist_ok=True)
os.makedirs(READY_DIR, exist_ok=True)

def cmd(args):
    return subprocess.run([ADB, "-s", SERIAL] + args, capture_output=True, text=True)

def ensure_app():
    # Make sure app is focused
    cmd(["shell", "monkey", "-p", "com.apnasandesh.app", "-c", "android.intent.category.LAUNCHER", "1"])
    time.sleep(1.0)

def tap(x, y, delay=1.2):
    cmd(["shell", "input", "tap", str(x), str(y)])
    time.sleep(delay)

def swipe(x1, y1, x2, y2, duration=300, delay=1.2):
    cmd(["shell", "input", "swipe", str(x1), str(y1), str(x2), str(y2), str(duration)])
    time.sleep(delay)

def key_back(delay=1.0):
    cmd(["shell", "input", "keyevent", "4"])
    time.sleep(delay)

def capture_raw(filename):
    path = os.path.join(RAW_DIR, filename)
    with open(path, "wb") as f:
        p = subprocess.Popen([ADB, "-s", SERIAL, "exec-out", "screencap", "-p"], stdout=f)
        p.wait()
    time.sleep(0.5)
    print(f"Captured RAW: {filename}")

# Enable clean demo mode
cmd(["shell", "settings", "put", "global", "sysui_demo_allowed", "1"])
cmd(["shell", "am", "broadcast", "-a", "com.android.systemui.demo", "-e", "command", "enter"])
cmd(["shell", "am", "broadcast", "-a", "com.android.systemui.demo", "-e", "command", "clock", "-e", "hhmm", "0941"])
cmd(["shell", "am", "broadcast", "-a", "com.android.systemui.demo", "-e", "command", "battery", "-e", "level", "100", "-e", "plugged", "false"])
cmd(["shell", "am", "broadcast", "-a", "com.android.systemui.demo", "-e", "command", "notifications", "-e", "visible", "false"])

ensure_app()

# 1. Dashboard
tap(72, 1550, 1.0)
capture_raw("01_Dashboard.png")

# 2. Campaign Composer (top)
tap(216, 1550, 1.2)
capture_raw("02_Campaign_Composer.png")

# 3. Campaign Composer (scrolled to preview bubble & segment calculator)
swipe(360, 1200, 360, 600, 300, 1.2)
capture_raw("03_Live_SMS_Preview.png")

# 4. Recipients Selection
tap(360, 1330, 1.5)
capture_raw("04_Recipients_Selection.png")

# 5. Pre-flight Review
tap(360, 1510, 1.5)
capture_raw("05_PreFlight_Review.png")

# Return to base tabs using top back arrow
tap(72, 140, 1.0)
tap(72, 140, 1.0)

# 6. Customers Directory
tap(360, 1550, 1.2)
capture_raw("06_Customers_Directory.png")

# 7. Customer Import & Export
tap(440, 270, 1.2)
capture_raw("07_Customer_Import_Export.png")
tap(72, 140, 1.0) # back to customers

# 8. Templates Library
tap(504, 1550, 1.2)
capture_raw("08_Template_Library.png")

# 9. Settings (tap settings icon at top right)
tap(675, 140, 1.2)
capture_raw("09_Settings_Engine.png")

# 10. Business Profile (tap Edit in business profile card)
tap(582, 311, 1.2)
capture_raw("10_Business_Profile.png")
tap(72, 140, 1.0) # back to settings
tap(72, 140, 1.0) # back to dashboard

print("All 10 RAW screenshots captured successfully!")

# Now generate Play Store ready presentation graphics (1080x1920, 9:16)
METADATA = [
    ("01_Dashboard.png", "100% Private Business SMS", "Aapka Number, Aapke Customers • Zero Cloud"),
    ("02_Campaign_Composer.png", "Smart Campaign Builder", "Direct SIM & Carrier Rate Pacing Controls"),
    ("03_Live_SMS_Preview.png", "Live Interactive SMS Preview", "GSM-7 & Unicode Character & Segment Counter"),
    ("04_Recipients_Selection.png", "Targeted Recipient Selection", "Filter Groups with Permanent Opt-Out Protection"),
    ("05_PreFlight_Review.png", "Telecom Regulatory Safeguards", "Built-in TRAI Guidance & Message Inspection"),
    ("06_Customers_Directory.png", "On-Device Business CRM", "Organized Directory with Consent & Custom Tags"),
    ("07_Customer_Import_Export.png", "Instant CSV & Contacts Import", "Bulk Import from Phone Contacts or CSV Files"),
    ("08_Template_Library.png", "40+ Ready Business Templates", "Retail, Festival Greetings & Payment Reminders"),
    ("09_Settings_Engine.png", "Flexible Messaging Transport", "Toggle Direct SIM or System SMS Composer"),
    ("10_Business_Profile.png", "Custom Store Branding", "Auto Signatures & Dynamic Placeholders")
]

W, H = 1080, 1920
font_title = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf", 46)
font_sub = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", 26)

for raw_name, title, subtitle in METADATA:
    bg = Image.new("RGB", (W, H), "#F4F7FB")
    draw = ImageDraw.Draw(bg)

    # Top brand gradient header
    for y in range(0, 360):
        factor = y / 360.0
        r = int(14 + (244 - 14) * (factor ** 1.8))
        g = int(82 + (247 - 82) * (factor ** 1.8))
        b = int(219 + (251 - 219) * (factor ** 1.8))
        draw.line([(0, y), (W, y)], fill=(r, g, b))

    # Center text
    bbox_t = font_title.getbbox(title)
    w_t = bbox_t[2] - bbox_t[0]
    draw.text(((W - w_t) // 2, 75), title, fill="#0B2253", font=font_title)

    bbox_s = font_sub.getbbox(subtitle)
    w_s = bbox_s[2] - bbox_s[0]
    draw.text(((W - w_s) // 2, 145), subtitle, fill="#4A5568", font=font_sub)

    # Load and scale screenshot
    sc_path = os.path.join(RAW_DIR, raw_name)
    sc = Image.open(sc_path)
    phone_w = 760
    aspect = sc.height / sc.width
    phone_h = int(phone_w * aspect)
    sc_resized = sc.resize((phone_w, phone_h), Image.Resampling.LANCZOS)

    # Rounded mask
    mask = Image.new("L", (phone_w, phone_h), 0)
    m_draw = ImageDraw.Draw(mask)
    m_draw.rounded_rectangle([(0, 0), (phone_w, phone_h)], radius=36, fill=255)

    # Shadow
    shadow_w, shadow_h = phone_w + 40, phone_h + 40
    shadow = Image.new("RGBA", (shadow_w, shadow_h), (0, 0, 0, 0))
    s_draw = ImageDraw.Draw(shadow)
    s_draw.rounded_rectangle([(20, 20), (phone_w + 20, phone_h + 20)], radius=40, fill=(0, 0, 0, 45))
    shadow = shadow.filter(ImageFilter.GaussianBlur(15))

    pos_x = (W - phone_w) // 2
    pos_y = 230

    bg.paste(shadow, (pos_x - 20, pos_y - 10), shadow)
    bg.paste(sc_resized, (pos_x, pos_y), mask)

    # Border
    border_img = Image.new("RGBA", (phone_w, phone_h), (0, 0, 0, 0))
    b_draw = ImageDraw.Draw(border_img)
    b_draw.rounded_rectangle([(0, 0), (phone_w - 1, phone_h - 1)], radius=36, outline=(200, 210, 225, 255), width=3)
    bg.paste(border_img, (pos_x, pos_y), border_img)

    out_name = raw_name.replace(".png", "_PlayStore.png")
    out_path = os.path.join(READY_DIR, out_name)
    bg.save(out_path)
    print(f"Generated Play Store Card: {out_name}")

print("All Play Store graphics generated successfully!")
