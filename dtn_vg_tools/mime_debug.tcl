#!/usr/bin/tclsh
#
#    Copyright 2007 Intel Corporation
#
#    Licensed under the Apache License, Version 2.0 (the "License");
#    you may not use this file except in compliance with the License.
#    You may obtain a copy of the License at
#
#        http://www.apache.org/licenses/LICENSE-2.0
#
#    Unless required by applicable law or agreed to in writing, software
#    distributed under the License is distributed on an "AS IS" BASIS,
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#    See the License for the specific language governing permissions and
#    limitations under the License.
#

package require http
package require mime
package require ncgi

load libdtntcl[info sharedlibextension] dtnapi

#----------------------------------------------------------------------
proc shift { l } {
    upvar $l xx
    set xx [lrange $xx 1 end]
}

#----------------------------------------------------------------------
proc arg0 { l } {
    return [lindex $l 0]
}

#----------------------------------------------------------------------
proc dbg {args} {
    global opt
    if {$opt(verbose)} {
        set nonewline ""
        if {[arg0 $args] == "-nonewline"} {
            set nonewline -nonewline
            shift args
        }

        set chan stdout
        if {[llength $args] == 2} {
            set chan [arg0 $args]
            shift args
        }
        
        set msg [arg0 $args]
        if {$nonewline != ""} {
            puts $nonewline $chan $msg
        } else {
            puts $chan $msg
        }
    }
}

#----------------------------------------------------------------------
proc usage {} {
    puts "Usage: dtnhttpproxy \[Options\]"
    puts ""
    puts "Options:"
    puts "    -h | --help        Print help message"
    puts "    -v | --verbose     Verbose mode"
    puts "    -e | --eid <eid>   Set eid (default is dtn://<local_eid>/http)"
}

#----------------------------------------------------------------------
proc init { argv } {
    global opt
    set opt(verbose) 0
    set opt(eid)     ""
    
    # parse options
    while {[llength $argv] > 0} {
        switch -- [arg0 $argv] {
            -h            -
            --help        { usage; exit }
            -v            -
            --verbose     { set opt(verbose) 1 }
            -e            -
            --eid         { shift argv; set opt(eid) [arg0 $argv] }
            default       { puts "illegal option \"[arg0 $argv]\""; usage; exit }
        }
        shift argv
    }
}

#----------------------------------------------------------------------
proc connect {} {
    global handle
    set handle [dtn_open]
    if {$handle == -1} {
        error "error in dtn_open_handle"
    }

    dbg "handle is $handle"
}

#----------------------------------------------------------------------
proc register {} {
    global handle opt regid DTN_REG_DEFER

    set eid $opt(eid)
    if {$eid == ""} {
        set eid [dtn_build_local_eid $handle "geocam"]
        if {$eid == ""} {
            error "error in dtn_build_local_eid: [dtn_strerror [dtn_errno $handle]]"
        }
    }
    dbg "eid is $eid"

    set regid [dtn_find_registration $handle $eid]
    if {$regid != -1} {
        dbg "found existing registration -- id $regid, calling bind..."
        dtn_bind $handle $regid
    } else {
        set regid [dtn_register $handle $eid $DTN_REG_DEFER 3600 false ""]
        dbg "created new registration -- id $regid"
    }
}

proc parseMimeValue {value} {
    set parts [split $value \;]
    set results [list [string trim [lindex $parts 0]]]
    set paramList [list]
    foreach sub [lrange $parts 1 end] {
	if {[regexp -- {([^=]+)=(.+)} $sub match key val]} {
            set key [string trim [string tolower $key]]
            set val [string trim $val]
            # Allow single as well as double quotes
            if {[regexp -- {^["']} $val quote]} { ;# need a " for balance
	       	      if {[regexp -- ^${quote}(\[^$quote\]*)$quote $val x val2]} {
			  # Trim quotes and any extra crap after close quote
			  set val $val2
		      }
	    }
		lappend paramList $key $val
        }
     }
     if {[llength $paramList]} {
		    lappend results $paramList
		}
		return $results
	    }	
#----------------------------------------------------------------------
proc flattenMime {message} {
    set result [list]
    if {[catch {
        set children [mime::getproperty $message parts]
    }]} {
        # getting parts failed - it's a leaf
        # in this case, get Content-ID header
        # and compare it to requested filename
	# dbg "We have a leaf"
	# lappend result $content
        if {![catch {
            set contentID [mime::getheader \
			       $message "Content-Disposition"]
        }]} {
            # if filename matches Content-ID
	    set content [mime::getbody $message]
	    set header [parseMimeValue $contentID]
	    set key [lindex [lindex $header 1] 1]
	    # dbg $header
	    # dbg $key 
	    lappend result $key $content
	    if {[string equal $key "photo"]} {
		set fileName [lindex [lindex $header 1] 3]
		#dbg "Filename is $fileName"
		lappend result filename $fileName
	    }
        }
    }  else {
        # for multipart/*, we'll iterate over children
        # to see if any of them contains the attachment
	dbg "Children $children"
        foreach token $children {
	    # dbg "token is $token" 
	    # dbg -nonewline "Body is " 
	    # dbg [mime::getbody $token]
            set result [concat $result \
                [flattenMime $token] \
                ]
        }
    }
    return $result
}

#----------------------------------------------------------------------
proc writeImage {filename data} {
   if [catch {open $filename w} fileId] {
	puts stderr "Cannot open $filename : $fileId"
    } else {
	puts -nonewline $fileId $data
	flush $fileId
	close $fileId
    }
}

proc saveParts {parts} {
    set pwd [file dirname [info script]]
    # lets create the output dir
    set dir [file join $pwd "out"]
    file mkdir $dir
    set idx [expr [lsearch -exact $parts "uuid"] + 1 ]
    set uuid [lindex $parts $idx]
    set idx [expr [lsearch -exact $parts "filename"] + 1 ]
    set fileName [lindex $parts $idx]

    #dbg "$uuid"
    set outputFile [file join $dir ${uuid}.txt]
    dbg "Set outputfile $outputFile"
    if [catch {open $outputFile w} fileId] {
	puts stderr "Cannot opeb $outputFile : $fileId"
    } else {
	foreach {key value} $parts {
	    #dbg "$key $value"
	    if {[string equal $key "photo"]} {
		puts "Photo found"
		set photoFile [file join $dir $fileName]
		writeImage $photoFile $value
	    } else {
		puts $fileId "$key $value"
	    }
	}
	flush $fileId
	close $fileId
    }
}

#----------------------------------------------------------------------
proc proxy_loop {} {
    set payload_file "./sample1.txt"
    set fd [open $payload_file r]
    set message [read $fd]
    # Let try and parse the mime data 
    dbg $message

    # dbg [ncgi::multipart "Content-Type: multipart/form-data; boundary=----------------31415926535", $message]


    # set token ""

    if {[catch {
    	#set token [mime::initialize -canonical "multipart/form-data" -encoding base64 -file $payload_file]
    	set token [mime::initialize -file $payload_file]
    	# set token [mime::initialize -file $payload_file]
    } error]} {
    	dbg "Error while parsing message: $error"
    }
    dbg $token
    set parts [flattenMime $token]
    #dbg -nonewline "Parts:$parts "
    saveParts $parts
    #dbg "Parts: $parts "
    #parray result
    # dbg $parts
    #set t [mime::getproperty $token]
    #dbg -nonewline "t is "
    #dbg " Properties: $t"
    # set body[mime::getbody $token]
    # dbg "Body is $body"
    flush stdout
}


proc http_progress {token total current} {
   dbg -nonewline .
   flush stdout
}

#----------------------------------------------------------------------
init $argv

#while {1} {
#    if [catch {
        # connect
        # register
        proxy_loop
#    } err] {
#        puts "$err"
#    }
#    catch {dtn_close $handle} err
#    after 5000
#}

