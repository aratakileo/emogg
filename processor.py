from shutil import copytree, copy2, rmtree
from os.path import isfile, isdir
from os import remove, getenv
from glob import glob


data = {}


def main():
    data['prev_mod_version'] = get_build_properties_value('prev_mod_version')
    data['mod_version'] = get_build_properties_value('mod_version')

    print(data)

    execute_root_cmd()


def execute_root_cmd():
    while True:
        print('Choose what to do:')
        print_option(1, 'inject built-in emojis to source code')
        print_option(2, 'apply mod to mods dir')
        print_exit_option()

        input_value = get_user_input()

        if input_value == '1':
            execute_builtin_emojis_injector()
        elif input_value == '2':
            execute_apply_mod_to_mods_dir()
        elif input_value == '':
            return
        else:
            print('Wrong answer!')


def execute_builtin_emojis_injector():
    print("Process is starting...")

    emoji_root_dir = 'src/main/resources/assets/emogg/emoji/'
    emoji_subdirs = ('*', '*/*')

    START_ANCHOR = '//!START'
    END_ANCHOR = '//!END'
    CODE_FILE_PATH = 'src/main/java/pextystudios/emogg/handler/EmojiHandler.java'

    with open(CODE_FILE_PATH, 'r', encoding='utf-8') as r:
        code_text = r.read()

        start_anchor_index = code_text.index(START_ANCHOR)
        space_offset = code_text[:start_anchor_index][::-1].index('\n')

        amount = 0
        code_injection = ''

        for subdir in emoji_subdirs:
            for emoji_path in glob(emoji_root_dir + subdir):
                emoji_path = emoji_path.replace('\\', '/')[len(emoji_root_dir):]

                if emoji_path.endswith('.gif') or emoji_path.endswith('png'):
                    code_injection += '\n' + ' ' * space_offset \
                                      + f'emoji = Emoji.from(' \
                                        f'new ResourceLocation(Emogg.NAMESPACE, "{"emoji/" + emoji_path}")' \
                                        f');\n' + ' ' * space_offset + f'builtinEmojis.put(emoji.getName(), emoji);'

                    amount += 1

        print(f'  Total found: {amount}')

        with open(CODE_FILE_PATH, 'w', encoding='utf-8') as w:
            w.write(
                code_text[:start_anchor_index + len(START_ANCHOR)] + code_injection
                + '\n' + ' ' * space_offset + code_text[code_text.index(END_ANCHOR):]
            )

    print('  Successfully applied!')


def execute_apply_mod_to_mods_dir():
    OLD_VERSION_FILE_NAME = 'emogg-' + data['prev_mod_version'] + '.jar'
    CURRENT_VERSION_FILE_NAME = 'emogg-' + data['mod_version'] + '.jar'
    MODS_DIR = getenv('APPDATA') + '/.minecraft/mods/'
    BUILD_DIR = 'build/libs/'

    for file_name in (OLD_VERSION_FILE_NAME, CURRENT_VERSION_FILE_NAME):
        if isfile(MODS_DIR + file_name):
            remove(MODS_DIR + file_name)

    copy2(BUILD_DIR + CURRENT_VERSION_FILE_NAME, MODS_DIR)

    print('Successfully applied!')


def get_build_properties_value(key: str) -> str | None:
    with open('gradle.properties', 'r', encoding='utf-8') as f:
        properties_text = f.read()
        properties_text = properties_text[properties_text.index(key) + len(key):]
        properties_text = properties_text[properties_text.index('=') + 1:]
        properties_text = properties_text.split('\n')[0]

        return properties_text.strip()


def get_user_input():
    return input('you:~# ')


def print_option(option_key: any, option_description: str):
    print(f'  {option_key} - {option_description}')


def print_exit_option():
    print_option('[enter]', 'exit')


main()
