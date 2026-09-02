#!/usr/bin/env python3
"""Deep-fix compressed Kotlin files by restoring newlines inside function bodies."""
import re
import sys

BODY_KEYWORDS = [
    'LaunchedEffect', 'BackHandler', 'DisposableEffect', 'SideEffect',
    'Scaffold', 'Column', 'Row', 'Box', 'Surface', 'Card',
    'AnimatedContent', 'AnimatedVisibility', 'HorizontalPager',
    'LazyColumn', 'LazyRow', 'FlowRow', 'FlowColumn',
    'ModalBottomSheet', 'Dialog', 'AlertDialog',
    'rememberUpdatedState', 'rememberSaveable',
    'snapshotFlow', 'coroutineScope',
]

def fix_deeper(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        lines = f.readlines()

    new_lines = []
    for line in lines:
        stripped = line.rstrip()

        # Fix 1: } followed by spaces followed by identifier → insert newline
        fixed = re.sub(r'\}(\s{2,})([A-Za-z@])', r'}\n\2', stripped)

        # Fix 2: Specific composable/block keywords that appear mid-line
        for kw in BODY_KEYWORDS:
            fixed = re.sub(r'(\s{2,})(' + re.escape(kw) + r')', r'\n\2', fixed)

        # Fix 3: if/when/for/while/try appearing after spaces mid-line
        fixed = re.sub(r'(\s{4,})(if \(|when \(|for \(|while \(|try \{)', r'\n\2', fixed)

        new_lines.append(fixed + '\n')

    result = ''.join(new_lines)
    after = result.count('\n')

    with open(filepath, 'w', encoding='utf-8', newline='\n') as f:
        f.write(result)
    print(f"  {filepath}: {len(lines)} -> {after} lines")


if __name__ == '__main__':
    for fp in sys.argv[1:]:
        print(f"Processing: {fp}")
        fix_deeper(fp)
