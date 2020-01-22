#!/bin/bash

SETTINGS_PATH=$(find ~ -name "config" | grep "\.AndroidStudio...")
if [[ $? == 1 ]]
then
echo "Can't find Android Studio's file. This file usually resides in HOME and goes by the name of .AndroidStudiox.x, where x.x is the version number. Please add the checkstyle plugin manually from the marketplace. Search for Checkstyle-IDEA."
exit
fi

PLUGINS_PATH="$SETTINGS_PATH/plugins"
STYLES_PATH="$SETTINGS_PATH/codestyles"

if [[ -e "$PLUGINS_PATH" ]];
then
echo "Plugins folder exists."
else
mkdir $PLUGINS_PATH
fi

if [[ -e "$STYLES_PATH" ]]
then
echo "Styles folder exists."
else
mkdir $STYLES_PATH
fi

#downloading the checkstyle plugin
wget https://plugins.jetbrains.com/files/1065/75335/CheckStyle-IDEA-5.35.2.zip -O temp.zip && unzip temp.zip -d $PLUGINS_PATH/CheckStyle-IDEA
if [[ $? == 1 ]]
then
echo "Failure in downloading checkstyle plugin. Please do it manually from the marketplace. Search for Checkstyle-IDEA. Alternatively, if you don't have wget or unzip installed, download them."
exit
fi
rm temp.zip

#copying the styles file
#mv google_checks.xml $STYLES_PATH/Checkstyle.xml

exit 0
