#!/bin/bash

# USAGE 1: start a process and monitor its stdout, showing progress bar
# monitor.sh <target> [marker_file] [working_directory]
# Parameters:
# 1. path to (executable) target (e.g. ./mtwilson-server.bin)
#    REQUIRED.
# 2. path to progress marker file (e.g. ./mtwilson-server.bin.mark)
#    OPTIONAL. Default is .mark extension after target filename.
# 3. path to working directory (e.g. /tmp/cit/monitor/mtwilson-server.bin)
#    OPTIONAL. Default is /tmp/cit/monitor/ followed by target filename.
# 
# If the target is a file,
# this script automatically chmod +x the target so it's ok
# if it's not already executable. The working directory is automatically
# created using mkdir -p. 

# The target can also be any executable on the path

# USAGE 2: check the status of a monitored process
# monitor.sh --status <working_directory>
# Parameters:
# 1. working directory , for example /tmp/cit/monitor/target.bin

# USAGE 3: monitor a process that is already executing
#          (do not start it, just look for the status updates and display progress bar)
# monitor.sh --noexec <working_directory>           if not currently running, the monitor will exit with a message
# monitor.sh --noexec --wait <working_directory>    if not currently running, the monitor will wait for it indefinitely

# USAGE 2: get the pid of a monitored process
# monitor.sh --pid <working_directory>
# Parameters:
# 1. working directory , for example /tmp/cit/monitor/target.bin

# A progress marker file contains an ORDERED LIST of UNIQUE lines to
# match in the output of the target. If a line is not unique, only
# the first occurrence will be used as the marker.
# This script executes the target, redirects the output to a
# temporary file, and looks for the markers in the output. Progress
# is updated based on which markers have been found in the output so
# far. The marker file must be an ORDERED LIST because once a marker
# is found, the progress is updated and the script only looks for
# subquent markers in the output. 

# ensure we have paths to standard binaries
export PATH=$PATH:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin

# useful constant for the green color at 100%
TERM_COLOR_GREEN="\\033[1;32m"
TERM_COLOR_CYAN="\\033[1;36m"
TERM_COLOR_RED="\\033[1;31m"
TERM_COLOR_YELLOW="\\033[1;33m"
TERM_COLOR_NORMAL="\\033[0;39m"

echo_success() {
  if [ "$TERM_DISPLAY_MODE" = "color" ]; then echo -en "${TERM_COLOR_GREEN}"; fi
  echo ${@:-"[  OK  ]"}
  if [ "$TERM_DISPLAY_MODE" = "color" ]; then echo -en "${TERM_COLOR_NORMAL}"; fi
  return 0
}

echo_failure() {
  if [ "$TERM_DISPLAY_MODE" = "color" ]; then echo -en "${TERM_COLOR_RED}"; fi
  echo ${@:-"[FAILED]"}
  if [ "$TERM_DISPLAY_MODE" = "color" ]; then echo -en "${TERM_COLOR_NORMAL}"; fi
  return 1
}

echo_warning() {
  if [ "$TERM_DISPLAY_MODE" = "color" ]; then echo -en "${TERM_COLOR_YELLOW}"; fi
  echo ${@:-"[WARNING]"}
  if [ "$TERM_DISPLAY_MODE" = "color" ]; then echo -en "${TERM_COLOR_NORMAL}"; fi
  return 1
}

echo_info() {
  if [ "$TERM_DISPLAY_MODE" = "color" ]; then echo -en "${TERM_COLOR_CYAN}"; fi
  echo ${@:-"[INFO]"}
  if [ "$TERM_DISPLAY_MODE" = "color" ]; then echo -en "${TERM_COLOR_NORMAL}"; fi
  return 1
}


parse_args() {
if [ "x$*" == "x" ] || [ "$1" == "--help" ]; then
  echo "Usage: monitor.sh target.bin [target.bin.mark] [/tmp/cit/monitor/target.bin]"
  exit 1
elif [ "$1" == "--status" ]; then
  shift
  if [ -n "$1" ] && [ -d "$1" ] && [ -f "$1/status" ]; then
    workdir=$1
    cat $workdir/status
    exit 0
  fi
  exit 1
elif [ "$1" == "--pid" ]; then
  shift
  if [ -n "$1" ] && [ -d "$1" ] && [ -f "$1/pid" ]; then
    workdir=$1
    target_pid=$(cat $workdir/pid 2>/dev/null)
    if is_target_running $target_pid; then echo $target_pid; fi
    exit 0
  fi
  exit 1
elif [ "$1" == "--noexec" ]; then
  MONITOR_NOEXEC=yes
  shift
  if [ "$1" == "--wait" ]; then
    MONITOR_NOEXEC_WAIT=yes
    shift
  fi
  if [ -n "$1" ] && [ -d "$1" ] && [ -f "$1/status" ]; then
    workdir=$1
    target=$(basename $1)
    markerfile=

    if is_target_active_running; then
      monitor_target
      exit $?
    elif [ "$MONITOR_NOEXEC_WAIT" ]; then
      monitor_target
      exit $?
    fi
  fi
  echo "target is not currently running"
  exit 1
else
  target=$1
  markerfile=$2
  workdir=$3
  # regular operation
  create_workdir
  init_markers
  execute_target
  monitor_target
  exit $?
fi
}

create_workdir() {
  if [ -f "$target" ] && [ -z "$markerfile" ]; then
    markerfile=${target}.mark
  elif [ ! -f $target ] && [ -z "$markerfile" ]; then
    is_target_on_path=$(which $target 2>/dev/null)
    if [ -n "$is_target_on_path" ]; then
      echo "Target '$target' is on path but marker file not specified"
      exit 1
    fi
  fi
  if [ -z "$workdir" ]; then
    filename=$(basename $target)
    workdir=/tmp/cit/monitor/$filename
  fi 
  mkdir -p $workdir/markers
  echo "PENDING" > $workdir/status
}

init_markers() {
  # split the marker file into individual numbered lines in separate files
  # the grep removes empty lines
  max=$(cat $markerfile | grep -v -e '^$' | wc -l)
  for i in `seq 1 $max`
  do
    cat $markerfile | grep -v -e '^$' | head -n $i | tail -n 1 > $workdir/markers/$i
  done
  echo $max > $workdir/max
  echo 0 > $workdir/progress
}

execute_target() {
  # if the current directory is not on the path and we just 
  # run "xyz" file in current directory, the shell will not find it.
  # but if we run "./xyz" the shell will find it.  so using
  # dirname and basename here to turn "xyz" into "./xyz" while
  # keeping "/root/xyz" as "/root/xyz" in case an absolute path was
  # provided.
  if [ -f "$target" ]; then chmod +x $target; fi
  #target_dirname=$(dirname $target)
  #target_basename=$(basename $target)
  $target 1>$workdir/stdout 2>$workdir/stderr  &
  target_pid=$!
  echo "ACTIVE" > $workdir/status
  echo $target_pid > $workdir/pid
}

is_target_running() {
    local target_pid="$1"
    if [ -n "$target_pid" ]; then
        local pid_status=$(ps --pid $target_pid -o pid= -o comm= | awk '{print $1}')
        if [ -n "$pid_status" ]; then return 0; fi
    fi
    return 1
}

# postcondition:
#   status is ACTIVE and return code 0, or other status and return code 1
# 
is_target_active_running() {
  if [ -d "$workdir" ]; then
    status=$(cat $workdir/status 2>/dev/null)
    max=$(cat $workdir/max 2>/dev/null)
    target_pid=$(cat $workdir/pid 2>/dev/null)
    if [ "$status" == "ACTIVE" ] && is_target_running $target_pid; then
      return 0
    fi
  fi
  return 1
}

is_target_active_resumable() {
  if [ -d "$workdir" ]; then
    status=$(cat $workdir/status 2>/dev/null)
    max=$(cat $workdir/max 2>/dev/null)
    target_pid=$(cat $workdir/pid 2>/dev/null)
    if [ "$status" == "ACTIVE" ] && ! is_target_running $target_pid; then
      return 0
    fi
  fi
  return 1
}


#  if [ ! -d "$workdir" ]; then
#    echo "Directory does not exist: $workdir"
#    return 1
#  fi
#  if [ "$status" == "DONE" ]; then
#    echo "$target is already done"
#    return 1
#  elif [ "$status" == "ERROR" ]; then
#    echo "$target stopped because of an error"
#    return 1
#  elif [ "$status" == "PENDING" ]; then
#    echo "$target has not yet started"
#    return 1
#  elif [ "$status" == "ACTIVE" ]; then
#    # ensure the pid is running
#    if is_target_running $target_pid; then
#      return 0
#    else
#      echo "$target is not running"
#      return 1
#    fi
#  else
#    echo "$target status is unknown"
#    return 1
#  fi



# requires global vars 'percent' and 'bar_width' to be set
progress_bar() {
  local bricks
  local spaces
  let bricks=percent*bar_width/100
  let spaces=bar_width-bricks
  for i in `seq 1 $bricks`
  do
    echo -n "="
  done
  echo -n ">"
  for i in `seq 1 $spaces`
  do
    echo -n " "
  done
  echo -ne "$percent%\r"
}

# preconditions:
# * variables next and max are defined integers
# * workdir is defined and writable
# * there are 'max' sequentially numbered marker files in $workdir/markers
# postconditions:
# * variable progress is set to last completed (seen) marker sequence number
# * variable next is set to progress+1 as the next marker to search
# * variable percent is set to progress*100/max
# * file $workdir/progress is updated with value of variable progress
update_progress() {
  # we check each marker from the next one to the end
  # because if one is skipped due to an error, we
  # may still catch the next one.
  for i in `seq $next $max`
  do
    marker=$(cat $workdir/markers/$i 2>/dev/null)
    #echo "marker=$marker"
    found=$(grep -m 1 "$marker" $workdir/stdout)
    if [[ -n "$found" ]]; then
      let progress=i
      let next=progress+1
      let percent=progress*100/max
      echo $progress > $workdir/progress
    fi
  done
}


monitor_target() {

# we're looking for the following things:
# 1. check that the target is still running
# 2. identify the next marker in the output
progress=0
next=1
percent=0
bricks=0
spaces=0
bar_width=50


while [ $progress -lt $max ] && is_target_running; do
  # echo progress without newline and with escape symbols,
  # the backslash r puts the cursor at the beginning of the line
  progress_bar
  update_progress
  sleep 1
done

# final check, update, and display
update_progress
echo $progress > $workdir/progress
progress_bar

# remove the temporary marker files
rm -rf $workdir/markers

if [ -n "$MONITOR_NOEXEC" ]; then
  # wait until the target exits and writes the code
  while [ -z "$target_exit_code" ]; do
    if [ -f $workdir/exit ]; then
      target_exit_code=$(cat $workdir/exit)
    else
      sleep 1
    fi
  done
else
  # normal operation
  # either target finished successfully or exited early
  # check the exit code
  wait $target_pid
  target_exit_code=$?
  echo $target_exit_code > $workdir/exit
  if [ $target_exit_code -eq 0 ]; then
    echo "DONE" > $workdir/status
  else
    echo "ERROR" > $workdir/status
  fi
fi

if [ $target_exit_code -eq 0 ]; then
  echo -en "$TERM_COLOR_GREEN"
  progress_bar
  echo -en "$TERM_COLOR_NORMAL"
else
  echo -en "$TERM_COLOR_RED"
  progress_bar
  echo -en "$TERM_COLOR_NORMAL"
fi

# finish with a newline
echo

# exit with target's exit code
return $target_exit_code
}

parse_args
