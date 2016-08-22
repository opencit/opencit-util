#!/bin/bash

# parameter:
# 1. path to (executable) installer (e.g. ./mtwilson-server.bin)
#    REQUIRED.
# 2. path to progress marker file (e.g. ./mtwilson-server.bin.mark)
#    OPTIONAL. Default is .mark extension after installer filename.
# 3. path to working directory (e.g. /tmp/cit/monitor/mtwilson-server.bin)
#    OPTIONAL. Default is /tmp/cit/monitor/ followed by installer filename.
# 
# This script automatically chmod +x the installer so it's ok
# if it's not already executable. The working directory is automatically
# created using mkdir -p. 

# A progress marker file contains an ORDERED LIST of UNIQUE lines to
# match in the output of the installer. If a line is not unique, only
# the first occurrence will be used as the marker.
# This script executes the installer, redirects the output to a
# temporary file, and looks for the markers in the output. Progress
# is updated based on which markers have been found in the output so
# far. The marker file must be an ORDERED LIST because once a marker
# is found, the progress is updated and the script only looks for
# subquent markers in the output. 

installer=$1
markerfile=$2
workdir=$3

# ensure we have paths to standard binaries
export PATH=$PATH:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin

# useful constant for the green color at 100%
TERM_COLOR_GREEN="\\033[1;32m"
TERM_COLOR_RED="\\033[1;31m"
TERM_COLOR_NORMAL="\\033[0;39m"

if [[ -z "$markerfile" ]]; then
  markerfile=${installer}.mark
fi
if [ ! -f $installer ] || [ ! -f $markerfile ]; then
  echo "Usage: monitor.sh installer.bin [installer.bin.mark] [/tmp/cit/monitor/installer.bin]"
  exit 1
fi
if [[ -z "$workdir" ]]; then
  filename=$(basename $installer)
  workdir=/tmp/cit/monitor/$filename
fi 
mkdir -p $workdir/markers
echo "PENDING" > $workdir/status

# split the marker file into individual numbered lines in separate files
# the grep removes empty lines
max=$(cat $markerfile | grep -v -e '^$' | wc -l)
for i in `seq 1 $max`
do
  cat $markerfile | grep -v -e '^$' | head -n $i | tail -n 1 > $workdir/markers/$i
done
echo $max > $workdir/max
echo 0 > $workdir/progress

# if the current directory is not on the path and we just 
# run "xyz" file in current directory, the shell will not find it.
# but if we run "./xyz" the shell will find it.  so using
# dirname and basename here to turn "xyz" into "./xyz" while
# keeping "/root/xyz" as "/root/xyz" in case an absolute path was
# provided.
chmod +x $installer
installer_dirname=$(dirname $installer)
installer_basename=$(basename $installer)
$installer_dirname/$installer_basename 1>$workdir/stdout 2>$workdir/stderr  &
installer_pid=$!
echo "ACTIVE" > $workdir/status

# we're looking for the following things:
# 1. check that the installer is still running
# 2. identify the next marker in the output
progress=0
next=1
percent=0
bricks=0
spaces=0
bar_width=50

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

installer_running() {
  is_running=$(ps | awk '{print $1}' | grep "^$installer_pid$" | grep -v grep)
  if [ -n "$is_running" ]; then
    return 0
  fi
  return 1
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
    marker=$(cat $workdir/markers/$i)
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

while [ $progress -lt $max ] && installer_running; do
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

# either installer finished successfully or exited early
# check the exit code
wait $installer_pid
installer_exit_code=$?
if [ $installer_exit_code -eq 0 ]; then
  echo "DONE" > $workdir/status
  echo -en "$TERM_COLOR_GREEN"
  progress_bar
  echo -en "$TERM_COLOR_NORMAL"
else
  echo "ERROR" > $workdir/status
  echo -en "$TERM_COLOR_RED"
  progress_bar
  echo -en "$TERM_COLOR_NORMAL"
fi

# finish with a newline
echo

# exit with installer's exit code
exit $installer_exit_code
