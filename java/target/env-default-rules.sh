# Default rules for variable values that configure the Java server.
# It is intended to be executed at the end of env.sh, hence after the settings from an environment file
# and the user data have been applied. It checks various variables for their
# presence, and if no value is set for a variable after evaluating the base env.sh,
# the optional environment appended from http://releases.sapsailing.com/environments,
# and the optional user data from the EC2 environment, default values may be computed
# which may use other variable values that *have* been set.
if [ -z "${SERVER_NAME}" ]; then
  SERVER_NAME=MASTER
fi

# Message Queue hostname where to
# send messages for replicas (this server is master)
if [ -z "${REPLICATION_HOST}" ]; then
  REPLICATION_HOST=rabbit.internal.sapsailing.com
fi
# For the port, use 0 for the RabbitMQ default or a specific port that your RabbitMQ server is listening on
if [ -z "${REPLICATION_PORT}" ]; then
  REPLICATION_PORT=0
fi
# The name of the message queuing fan-out exchange that this server will use in its role as replication master.
# Make sure this is unique so that no other master is writing to this exchange at any time.
if [ -z "${REPLICATION_CHANNEL}" ]; then
  if [ -n "$AUTO_REPLICATE" ]; then
    # This seems to be a replica; use a dedicated outbound channel for "transitive replication"
    REPLICATION_CHANNEL=${SERVER_NAME}-${INSTANCE_NAME}
  else
    # This seems to be a master (or at best a replica only regarding SecurityService / SharedSailingData). User server name
    # as the outbound replication exchange name:
    REPLICATION_CHANNEL=${SERVER_NAME}
  fi
fi

if [ -z "${TELNET_PORT}" ]; then
  TELNET_PORT=14888
fi
if [ -z "${SERVER_PORT}" ]; then
  SERVER_PORT=8888
fi
if [ -z "${MONGODB_NAME}" ]; then
  MONGODB_NAME=${SERVER_NAME}
fi
if [ -z "${MONGODB_PORT}" ]; then
  MONGODB_PORT=27017
fi
if [ -z "${MONGODB_HOST}" -a -z "${MONGODB_URI}" ]; then
  MONGODB_URI="mongodb://mongo0.internal.sapsailing.com,mongo1.internal.sapsailing.com/${MONGODB_NAME}?replicaSet=live&retryWrites=true&readPreference=nearest"
fi
if [ -z "${EXPEDITION_PORT}" ]; then
  EXPEDITION_PORT=2010
fi
if [ -z "${REPLICATE_MASTER_SERVLET_HOST}" ]; then
  REPLICATE_MASTER_SERVLET_HOST=${SERVER_NAME}.sapsailing.com
fi
if [ -z "${REPLICATE_MASTER_SERVLET_PORT}" ]; then
  REPLICATE_MASTER_SERVLET_PORT=443
fi
# Host where RabbitMQ is running 
if [ -z "${REPLICATE_MASTER_QUEUE_HOST}" ]; then
  REPLICATE_MASTER_QUEUE_HOST=rabbit.internal.sapsailing.com
fi
# Port that RabbitMQ is listening on (normally something like 5672); use 0 to connect to RabbitMQ's default port
if [ -z "${REPLICATE_MASTER_QUEUE_PORT}" ]; then
  REPLICATE_MASTER_QUEUE_PORT=0
fi
# Exchange name that the master from which to auto-replicate is using as
# its REPLICATION_CHANNEL variable, mapping to the master's replication.exchangeName
# system property.
#
if [ -z "${REPLICATE_MASTER_EXCHANGE_NAME}" ]; then
  REPLICATE_MASTER_EXCHANGE_NAME=${SERVER_NAME}
fi
if [ -z "${BUILD_COMPLETE_NOTIFY}" ]; then
  export BUILD_COMPLETE_NOTIFY=simon.marcel.pamies@sap.com
fi
if [ -z "${SERVER_STARTUP_NOTIFY}" ]; then
  export SERVER_STARTUP_NOTIFY=simon.marcel.pamies@sap.com
fi
