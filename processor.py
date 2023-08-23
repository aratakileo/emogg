from os import remove, getenv
from os.path import isfile
from shutil import copy2


data = {}


def main():
    data['prev_mod_version'] = get_build_properties_value('prev_mod_version')
    data['mod_version'] = get_build_properties_value('mod_version')
    data['archives_base_name'] = get_build_properties_value('archives_base_name')

    try:
        data['support_minecraft_version'] = get_build_properties_value('support_minecraft_version')
    except Exception:
        data['support_minecraft_version'] = get_build_properties_value('minecraft_version')

    execute_root_cmd()


def execute_root_cmd():
    while True:
        print('Choose what to do:')
        print_option(1, 'apply mod to mods dir')
        print_exit_option()

        input_value = get_user_input()

        if input_value == '1':
            execute_apply_mod_to_mods_dir()
        elif input_value == '':
            return
        else:
            print('Wrong answer!')


def execute_apply_mod_to_mods_dir():
    OLD_VERSION_FILE_NAME = f'{data["archives_base_name"]}-{data["prev_mod_version"]}' \
                            f'+{data["support_minecraft_version"]}.jar'

    CURRENT_VERSION_FILE_NAME = f'{data["archives_base_name"]}-{data["mod_version"]}' \
                                f'+{data["support_minecraft_version"]}.jar'

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
