#!/usr/bin/env python3
"""
Restores newlines to a single compressed Kotlin code line.
Processes only the portion of the file that was compressed (typically the last "line").
"""

STMT_STARTERS = [
    # Top-level declarations (order matters - longer first!)
    'private data class ', 'internal data class ', 'data class ',
    'private sealed class ', 'sealed class ',
    'private enum class ', 'enum class ',
    'private abstract class ', 'abstract class ',
    'private class ', 'internal class ', 'class ',
    'companion object', 'private object ', 'internal object ', 'object ',
    'private interface ', 'interface ',
    'private fun ', 'internal fun ', 'public fun ', 'fun ',
    'private val ', 'internal val ', 'val ',
    'private var ', 'internal var ', 'var ',
    'abstract ', 'override ',
    'import ',
    '@file:', '@JvmField', '@JvmStatic',
]

def restore_single_line(code: str) -> str:
    """Restore newlines in a single compressed Kotlin code string."""
    result = []
    i = 0
    n = len(code)

    IN_NORMAL = 0
    IN_STRING = 1
    IN_TRIPLE = 2
    IN_BLOCK_COMMENT = 3
    IN_CHAR = 4

    state = IN_NORMAL
    escape_next = False
    string_depth = 0  # for ${ } inside strings

    def last_nonspace():
        for c in reversed(result):
            if c not in (' ', '\t', '\n', '\r'):
                return c
        return ''

    def check_and_insert_newline():
        remaining = code[i:]
        for starter in STMT_STARTERS:
            if remaining.startswith(starter):
                prev = last_nonspace()
                # Insert newline if previous token boundary is clear
                if prev in ('}', ')', ';', ','):
                    # Don't add newline if result already ends with newline
                    joined = ''.join(result)
                    if not joined.endswith('\n'):
                        result.append('\n')
                return
        
        # Handle @ annotations
        if remaining.startswith('@') and not remaining.startswith('@\"'):
            prev = last_nonspace()
            if prev in ('}', ')'):
                joined = ''.join(result)
                if not joined.endswith('\n'):
                    result.append('\n')

    while i < n:
        ch = code[i]
        ahead2 = code[i:i+2]
        ahead3 = code[i:i+3]

        if state == IN_BLOCK_COMMENT:
            result.append(ch)
            if ahead2 == '*/':
                result.append('/')
                i += 2
                state = IN_NORMAL
                continue
            i += 1
            continue

        if state == IN_TRIPLE:
            if ahead3 == '"""':
                result.append('"""')
                i += 3
                state = IN_NORMAL
                continue
            result.append(ch)
            i += 1
            continue

        if state == IN_STRING:
            if escape_next:
                result.append(ch)
                escape_next = False
                i += 1
                continue
            if ch == '\\':
                escape_next = True
                result.append(ch)
                i += 1
                continue
            if ch == '"':
                state = IN_NORMAL
            result.append(ch)
            i += 1
            continue

        if state == IN_CHAR:
            if escape_next:
                result.append(ch)
                escape_next = False
                i += 1
                continue
            if ch == '\\':
                escape_next = True
                result.append(ch)
                i += 1
                continue
            if ch == "'":
                state = IN_NORMAL
            result.append(ch)
            i += 1
            continue

        # NORMAL state
        if ahead3 == '"""':
            check_and_insert_newline()
            result.append('"""')
            i += 3
            state = IN_TRIPLE
            continue

        if ch == '"':
            check_and_insert_newline()
            result.append(ch)
            i += 1
            state = IN_STRING
            continue

        if ch == "'":
            result.append(ch)
            i += 1
            state = IN_CHAR
            continue

        if ahead2 == '/*':
            result.append('/*')
            i += 2
            state = IN_BLOCK_COMMENT
            continue

        # Check for statement starters
        check_and_insert_newline()

        result.append(ch)
        i += 1

    return ''.join(result)


def fix_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    lines = content.split('\n')
    total_before = len(lines)
    print(f"Lines before: {total_before}")

    fixed_lines = []
    for j, line in enumerate(lines):
        if len(line) > 500:  # Only process long "compressed" lines
            print(f"  Processing long line {j+1} ({len(line)} chars)...")
            restored = restore_single_line(line)
            restored_lines = restored.split('\n')
            print(f"  Expanded to {len(restored_lines)} lines")
            fixed_lines.extend(restored_lines)
        else:
            fixed_lines.append(line)

    fixed = '\n'.join(fixed_lines)
    total_after = fixed.count('\n') + 1
    print(f"Lines after: {total_after}")

    with open(filepath, 'w', encoding='utf-8', newline='\n') as f:
        f.write(fixed)
    print(f"Saved: {filepath}")


if __name__ == '__main__':
    import sys
    for f in sys.argv[1:]:
        print(f"\n=== {f} ===")
        fix_file(f)
