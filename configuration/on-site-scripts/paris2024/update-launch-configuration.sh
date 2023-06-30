#!/bin/bash
LAUNCH_CONFIGURATION_NAME_PATTERN="^paris2024-.*"
AUTO_SCALING_GROUP_NAME_PATTERN="^paris2024.*"
KEY_NAME=Axel

if [ $# -eq 0 ]; then
    echo "$0 -R <release-name> [-t <instance-type>] [-i <ami-id>] [-k <key-pair-name>]"
    echo ""
    echo "-i Amazon Machine Image (AMI) ID to use to launch the instance; defaults to latest image tagged with image-type:sailing-analytics-server"
    echo "-k Key pair name, mapping to the --key-name parameter"
    echo "-R release name; must be provided to select the release, e.g., build-202106040947"
    echo "-t Instance type; defaults to ${INSTANCE_TYPE}"
    echo
    echo "Example: $0 -R build-202106041327 -k Jan"
    echo
    echo "Will upgrade the auto-scaling group paris2024-* in the regions from regions.txt with a new"
    echo "launch configuration that will be derived from the existing launch configuration named paris2024-*"
    echo "by copying it to paris2024-{RELEASE_NAME} while updating the INSTALL_FROM_RELEASE parameter in the"
    echo "user data to the {RELEASE_NAME}, and optionally adjusting the AMI, key pair name and instance type if specified."
    echo "Note: this will NOT terminate any instances in the target group!"
    exit 2
fi
options='R:b:t:i:k:'
while getopts $options option
do
    case $option in
        i) IMAGE_ID=$OPTARG;;
        k) KEY_NAME=$OPTARG;;
        R) RELEASE=$OPTARG;;
        t) INSTANCE_TYPE=$OPTARG;;
        \?) echo "Invalid option"
            exit 4;;
    esac
done
for REGION in $( cat `dirname $0`/regions.txt ); do
  export AWS_DEFAULT_REGION=${REGION}
  echo "Starting auto-scaling group upgrade process for region ${REGION}"
  echo "----------------------------------------------------------------------"
  LAUNCH_CONFIGURATION_NAME=$( aws autoscaling describe-launch-configurations | jq -r '.LaunchConfigurations | map(select(.LaunchConfigurationName | test("'${LAUNCH_CONFIGURATION_NAME_PATTERN}'")))[].LaunchConfigurationName' | sort | tail -n 1)
  echo "Found launch configuration ${LAUNCH_CONFIGURATION_NAME}"
  LAUNCH_CONFIGURATION_JSON=$( aws autoscaling describe-launch-configurations --launch-configuration-name ${LAUNCH_CONFIGURATION_NAME} | jq -r '.LaunchConfigurations[0]' )
  OLD_USER_DATA=$( echo "${LAUNCH_CONFIGURATION_JSON}" | jq -r '.UserData' | base64 -d )
  if [ -z "${IMAGE_ID}" ]; then
    REGIONAL_IMAGE_ID=$( echo "${LAUNCH_CONFIGURATION_JSON}" | jq -r '.ImageId' )
  else
    REGIONAL_IMAGE_ID=${IMAGE_ID}
  fi
  if [ -z "${INSTANCE_TYPE}" ]; then
    REGIONAL_INSTANCE_TYPE=$( echo "${LAUNCH_CONFIGURATION_JSON}" | jq -r '.InstanceType' )
  else
    REGIONAL_INSTANCE_TYPE=${INSTANCE_TYPE}
  fi
  SECURITY_GROUP=$( echo "${LAUNCH_CONFIGURATION_JSON}" | jq -r '.SecurityGroups[0]' )
  BLOCK_DEVICE_MAPPINGS="$( echo "${LAUNCH_CONFIGURATION_JSON}" | jq -r '.BlockDeviceMappings' )"
  NEW_USER_DATA=$( echo "${OLD_USER_DATA}" | sed -e 's/^INSTALL_FROM_RELEASE=.*$/INSTALL_FROM_RELEASE='${RELEASE}'/' )
  NEW_LAUNCH_CONFIGURATION_NAME=paris2024-${RELEASE}
  echo "Creating new launch configuration ${NEW_LAUNCH_CONFIGURATION_NAME}"
  aws autoscaling create-launch-configuration --launch-configuration-name ${NEW_LAUNCH_CONFIGURATION_NAME} --image-id ${REGIONAL_IMAGE_ID} --key-name ${KEY_NAME} --security-groups ${SECURITY_GROUP} --user-data "${NEW_USER_DATA}" --instance-type ${REGIONAL_INSTANCE_TYPE} --block-device-mappings "${BLOCK_DEVICE_MAPPINGS}"
  EXIT_CODE=$?
  if [ "${EXIT_CODE}" = "0" ]; then
    echo "Creation of launch configuration ${NEW_LAUNCH_CONFIGURATION_NAME} successful. Continuing with updating the auto-scaling group"
    AUTO_SCALING_GROUP_NAME=$( aws autoscaling describe-auto-scaling-groups | jq -r '.AutoScalingGroups | map(select(.AutoScalingGroupName | test("'${AUTO_SCALING_GROUP_NAME_PATTERN}'")))[].AutoScalingGroupName' )
    echo "Found auto-scaling group ${AUTO_SCALING_GROUP_NAME}"
    aws autoscaling update-auto-scaling-group --auto-scaling-group-name ${AUTO_SCALING_GROUP_NAME} --launch-configuration-name ${NEW_LAUNCH_CONFIGURATION_NAME}
    EXIT_CODE=$?
    if [ "${EXIT_CODE}" = "0" ]; then
      echo "Updating auto-scaling group ${AUTO_SCALING_GROUP_NAME} seems to have completed successfully."
      echo "Removing old launch configuration ${LAUNCH_CONFIGURATION_NAME}"
      aws autoscaling delete-launch-configuration --launch-configuration-name ${LAUNCH_CONFIGURATION_NAME}
    else
      echo "Attempt to update the auto-scaling group returned exit status $? which is considered an error."
      echo "Removing new launch configuration ${NEW_LAUNCH_CONFIGURATION_NAME} again"
      aws autoscaling delete-launch-configuration --launch-configuration-name ${NEW_LAUNCH_CONFIGURATION_NAME}
      exit ${EXIT_CODE}
    fi
  else
    echo "Creating the new launch configuration ${NEW_LAUNCH_CONFIGURATION_NAME} failed with exit status ${EXIT_CODE}"
    exit ${EXIT_CODE}
  fi
done
