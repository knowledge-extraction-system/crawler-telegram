#!/bin/bash

# example:
#./scripts/db-migration/liquibase-add-migration.sh asd "$(cat << EOF
#content
#EOF
#)"
# or :
# ./scripts/db-migration/liquibase-add-migration.sh admin_oauth_token FIXME ./dev


# Get working directory. It is directory where this script is located
# http://stackoverflow.com/questions/59895/can-a-bash-script-tell-what-directory-its-stored-in
# Is a useful one-liner which will give you the full directory name
# of the script no matter where it is being called from
WORKING_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
ROOT_DIR="$WORKING_DIR/../.."

CHANGELOG_PATH="$ROOT_DIR/src/main/resources/liquibase/changelog"
DEFAULT_VERSION="$(($(date +%s%N)/1000000))"
NAME="$1"
CONTENT="$2"
RELATIVE_PATH="${3:-.}"
WRAP_MIGRATION="${4:-wrap}"
VERSION="${5:-$DEFAULT_VERSION}"

if [ x"${CONTENT}" == "x" ] ; then
   printf "usage: \n\t$0 <migration description> <content> [ <relative path> [ <wrap migration> ] ]\n"
   exit 1
fi

if [ "${CONTENT}" == "-" ] ; then
    CONTENT="$(cat)"
fi

CHANGELOG_FILE="$CHANGELOG_PATH/$RELATIVE_PATH/$(printf %014s "$VERSION" | sed 's/ /0/g')__${NAME}__${USER}.xml"
mkdir -p "$CHANGELOG_PATH/$RELATIVE_PATH"

function add-migration() {
    echo "writing to file ${CHANGELOG_FILE}"
    if [ "${WRAP_MIGRATION}" == "wrap" ] ; then
       HEADER="    <changeSet author=\""$USER"\" id=\""${VERSION}-1"\">"
       FOOTER='    </changeSet>'
    fi

    cat << EOF > $CHANGELOG_FILE
<?xml version="1.1" encoding="UTF-8" standalone="no"?>
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog" xmlns:ext="http://www.liquibase.org/xml/ns/dbchangelog-ext" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog-ext http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.5.xsd">
$HEADER
$CONTENT
$FOOTER
</databaseChangeLog>
EOF
    echo 'changelog written'
}

add-migration
