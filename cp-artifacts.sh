#!/bin/bash

MODS_PATH=~/starsector/mods/SSMSControllerEx
SSMSC_PATH=~/SSMSController/

rm -r "${MODS_PATH}"/*
cp -r "${SSMSC_PATH}/jars" "${MODS_PATH}"
cp -r "${SSMSC_PATH}/graphics" "${MODS_PATH}"
cp -r "${SSMSC_PATH}/data" "${MODS_PATH}"
cp -r "${SSMSC_PATH}/src" "${MODS_PATH}"
cp "${SSMSC_PATH}/mod_info.json" "${MODS_PATH}"
