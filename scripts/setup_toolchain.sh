#!/bin/zsh -l

echo "Please enter your username for github.tools.sap: "
read -r GH_USER_INPUT

echo "Please enter a personal access token for github.tools.sap: "
read -sr GH_PAT_INPUT

# Execute centrally managed script
/bin/zsh -c "$(curl -fsSL -u $GH_USER_INPUT:$GH_PAT_INPUT https://raw.github.tools.sap/SAPMobile/dev-setup/main/android-developer.sh)"

# Extend below if you like :)
