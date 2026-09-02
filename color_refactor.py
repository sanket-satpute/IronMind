import os
import re

D1_COLORS = {
    "DeepBackground": (13, 13, 18),
    "SurfaceDark": (22, 22, 30),
    "SurfaceElevated": (30, 30, 40),
    "NeonCyan": (0, 229, 255),
    "IronRed": (255, 69, 0),
    "ElectricViolet": (138, 43, 226),
    "GoldXP": (255, 215, 0),
    "SuccessGreen": (0, 255, 135),
    "ErrorRed": (255, 42, 85),
    "WarningAmber": (255, 176, 32),
    "TextPrimary": (255, 255, 255),
}

def hex_to_rgb(hex_str):
    if len(hex_str) == 8:
        # ARGB
        a = int(hex_str[0:2], 16)
        r = int(hex_str[2:4], 16)
        g = int(hex_str[4:6], 16)
        b = int(hex_str[6:8], 16)
        return a, r, g, b
    elif len(hex_str) == 6:
        r = int(hex_str[0:2], 16)
        g = int(hex_str[2:4], 16)
        b = int(hex_str[4:6], 16)
        return 255, r, g, b
    return 255, 0, 0, 0

def closest_color(r, g, b):
    min_dist = float('inf')
    best_token = None
    for token, (tr, tg, tb) in D1_COLORS.items():
        # Weighted distance to make grays match DeepBackground/Surface better
        dist = (r - tr)**2 + (g - tg)**2 + (b - tb)**2
        if dist < min_dist:
            min_dist = dist
            best_token = token
    return best_token

def replace_colors(content):
    # Matches Color(0xFFABCDEF) or Color(0xABCDEF)
    pattern = re.compile(r'Color\(\s*0x([0-9A-Fa-f]{6,8})\s*\)')
    
    def replacer(match):
        hex_val = match.group(1)
        a, r, g, b = hex_to_rgb(hex_val)
        token = closest_color(r, g, b)
        if a < 255:
            alpha_float = round(a / 255.0, 2)
            return f"{token}.copy(alpha = {alpha_float}f)"
        return token

    new_content, count = pattern.subn(replacer, content)
    return new_content, count

def main():
    base_dir = r"C:\Users\lenovo\AndroidStudioProjects\IronMind\app\src\main\java\com\sanket_satpute_20\ironmind"
    total_replaced = 0
    
    for root, dirs, files in os.walk(base_dir):
        # Skip the theme folder itself so we don't destroy Color.kt!
        if "ui\\theme" in root or "ui/theme" in root:
            continue
            
        for file in files:
            if file.endswith(".kt"):
                path = os.path.join(root, file)
                with open(path, 'r', encoding='utf-8') as f:
                    content = f.read()
                
                new_content, count = replace_colors(content)
                if count > 0:
                    # Also we need to inject imports if tokens were used
                    imports_needed = []
                    for t in D1_COLORS.keys():
                        if re.search(r'\b' + t + r'\b', new_content):
                            imports_needed.append(t)
                    
                    if imports_needed:
                        # Find last import
                        import_lines = []
                        last_import_idx = -1
                        lines = new_content.split('\n')
                        for i, line in enumerate(lines):
                            if line.startswith('import '):
                                last_import_idx = i
                        
                        if last_import_idx != -1:
                            for t in imports_needed:
                                import_str = f"import com.sanket_satpute_20.ironmind.ui.theme.{t}"
                                if import_str not in new_content:
                                    lines.insert(last_import_idx + 1, import_str)
                                    last_import_idx += 1
                            new_content = '\n'.join(lines)
                            
                    with open(path, 'w', encoding='utf-8') as f:
                        f.write(new_content)
                    total_replaced += count
                    print(f"Replaced {count} in {file}")
                    
    print(f"Total replacements: {total_replaced}")

if __name__ == '__main__':
    main()
