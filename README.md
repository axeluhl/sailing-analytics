<h1 align="center">sapsailing</h1>

<p align="center">
  <a href="#local-setup">Local Setup</a> •
  <a href="#azure-devops">Access Azure DevOps</a> •
  <a href="#running-the-app">Running the App</a>
</p>


## Local Setup

* Go to [github.tools.sap](https://github.tools.sap) and [create a PAT](https://docs.github.com/en/github/authenticating-to-github/keeping-your-account-and-data-secure/creating-a-personal-access-token) for your development machine
* If you're developing on a Mac:

    1. Toggle Privileges
    2. Open the repository in your preferred shell (`zsh` or `bash`)
    3. Run the subsequent command in the shell:

        ```sh
        scripts/setup_toolchain.sh
        ```
* If you're developing on Windows / Linux:
    1. Make sure to install a Ruby version manager ([RVM](https://rvm.io/rvm/install) or [rbenv](https://github.com/rbenv/rbenv))
    2. Install bundler by running `gem install bundler`
    3. Install fastlane's dependencies by running `bundle install` in the folder of your local clone of this repository

## Azure DevOps
If you want to have a closer look at the CI or want to download artifacts from there, please ask the development team to add you to the project on Azure DevOps. Before that, you need to request access to the Azure Portal. This request will be auto-approved and can be created on the [portal access page](https://myaccess.microsoft.com/@sap.onmicrosoft.com#/access-packages/d55ad7db-69af-4da5-8520-de187364513d)

Please use "Required for Azure DevOps" as the justification / reason.

## Running the App

1. Ask your colleagues to get added to Microsoft App Center
2. Open [sapsailing-android-apps on App Center](https://install.appcenter.ms/orgs/sapsailing/apps/sapsailing-android-apps) on your company phone
3. Sign in with your Microsoft account (SAP mail address)
4. Download the app and click "I'm good" if it asks you to enroll the device
5. It will then install the app.

For further information, please have a look [here](https://pages.github.tools.sap/SAPMobile/Documentation/howto/app-center/).
