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
    puts "    -u | --url <url>   Set url (default is http://localhost:8888/upload/guest/"
}

#----------------------------------------------------------------------
proc init { argv } {
    global opt
    set opt(verbose) 0
    set opt(eid)     ""
    set opt(url)     ""
    
    # parse options
    while {[llength $argv] > 0} {
        switch -- [arg0 $argv] {
            -h            -
            --help        { usage; exit }
            -v            -
            --verbose     { set opt(verbose) 1 }
            -e            -
            --eid         { shift argv; set opt(eid) [arg0 $argv] }
            -u            -
            --url         { shift argv; set opt(url) [arg0 $argv] }
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

#----------------------------------------------------------------------
proc proxy_loop {} {
    global handle opt DTN_PAYLOAD_FILE

    # use passed in url for http post or default
    set url $opt(url)
    if {$url == ""} {
        set url http://localhost:8888/upload/guest/
    }

    # http post content-type
    set content_type "multipart/form-data; boundary=--------multipart_formdata_boundary$--------"

    set i 0
    while {1} {
        set i [expr {$i + 1}]
        dbg "********\[bundle $i\] waiting on dtn_recv..."
        set bundle [dtn_recv $handle $DTN_PAYLOAD_FILE -1]
        if {$bundle == "NULL"} {
            error "error in dtn_recv: [dtn_strerror [dtn_errno $handle]]"
        }

        set source [dtn_bundle_source_get $bundle]
        set dest [dtn_bundle_dest_get $bundle]
        set payload_file [dtn_bundle_payload_get $bundle]
        set expiration [dtn_bundle_expiration_get $bundle]
        set creation_ts "[dtn_bundle_creation_secs_get $bundle].\
                [dtn_bundle_creation_seqno_get $bundle]"
        
        dbg "received bundle:"
        dbg "  source: $source"
        dbg "  dest: $dest"
        dbg "  expiration: $expiration"
        dbg "  creation_ts: $creation_ts"
        dbg "  payload: $payload_file"
        dbg "  payload_size: [file size $payload_file]"

	# open payload file and read to local variable
	set fsize [file size $payload_file]
        set fd [open $payload_file r]
        fconfigure $fd -translation binary
        set payload_data [read $fd $fsize]

        # for no particular reason, write the last bundle to file
	set lb [open "./last_bundle.txt" w]
        puts -nonewline $lb $payload_data

        # for no particular reason, save file with http response
        set outfd [open "./response.html" w]
        fconfigure $outfd -translation binary

	#debug
        #dbg "\n\n****PAYLOAD DATA:\n$payload_data"


        # http post
        if [catch {
            dbg -nonewline " calling http::geturl for $url..."
            set token [::http::geturl $url \
                      -type $content_type \
		      -binary true \
                      -blocksize 1024 \
                      -progress http_progress \
                      -channel $outfd \
                      -query $payload_data]

            dbg -nonewline " waiting..."
            ::http::wait $token
            dbg ""

            dbg " status: [::http::status $token]"
            dbg " error:  [::http::error $token]"
	    close $outfd

            # send return receipt bundle
            set resp [open "./response.html" r]
            send_receipt "<html>[read $resp]</html>" $source $dest
            close $resp
            
        } err] {
            puts "error in geturl: $err"
        }
        puts stderr ""
	close $fd
    }
}

proc http_progress {token total current} {
   dbg -nonewline .
   flush stdout
}

proc debug {} {
    global handle opt DTN_PAYLOAD_FILE

    # use passed in url for http post or default
    set url $opt(url)
    if {$url == ""} {
        set url http://localhost:8888/upload/guest/
    }

    # http post content-type
    set content_type "multipart/form-data; boundary=--------multipart_formdata_boundary$--------"

    set fsize [file size "./last_bundle.txt"]
    set fd [open "./last_bundle.txt" r]
    fconfigure $fd -translation binary
    set payload_data [read $fd $fsize]
    #set payload_data [join [split $payload_data \n]]
    #set lb [open "./last_bundle.txt" w]
    #puts -nonewline $lb $payload_data
    #dbg "\n\n****PAYLOAD DATA:\n$payload_data"

    set outfd [open "./response.html" w]
    fconfigure $outfd -translation binary


    # http post
    if [catch {
        dbg -nonewline " calling http::geturl for $url..."
        set token [::http::geturl $url \
                  -type $content_type \
		  -binary true \
                  -blocksize 1024 \
                  -progress http_progress \
                  -channel $outfd \
                  -query $payload_data]

        dbg -nonewline " waiting..."
        ::http::wait $token
        dbg ""

        dbg " status: [::http::status $token]"
        dbg " error:  [::http::error $token]"
            
    } err] {
        puts "error in geturl: $err"
    }
    puts stderr ""
    close $fd
}

#----------------------------------------------------------------------
proc uniq { filename } {
    # lets grab a number from the file name 
    # regexp {([:digit:]+)$} $filename idx
    set ext [file extension $filename]
    set idx 0
    set rootfilename [file rootname $filename]
    set tempname $rootfilename
    append tempname "-${idx}${ext}"
    while { [file exists $tempname] } {
	set tempname $rootfilename
	incr idx 1
	append tempname "-${idx}${ext}"
    }

    return $tempname
}

#----------------------------------------------------------------------
proc writeImage {filename data} {
   if [catch {open $filename w} fileId] {
	puts stderr "Cannot open $filename : $fileId"
    } else {
	fconfigure $fileId -translation binary -encoding binary
	puts -nonewline $fileId $data
	flush $fileId
	close $fileId
    }
}

#----------------------------------------------------------------------
proc saveParts {parts} {
    set pwd [file dirname [info script]]
    # lets create the output dir
    set dir [file join $pwd "out"]
    file mkdir $dir
    set idx [expr [lsearch -exact $parts "uuid"] + 1 ]
    set uuid [lindex $parts $idx]
    set idx [expr [lsearch -exact $parts "filename"] + 1 ]
    set fileName [lindex $parts $idx]

    set photoFile [uniq [file join $dir $fileName]]
    set uuid [file rootname $photoFile]

    #dbg "$uuid"
    set outputFile ${uuid}.txt

    # dbg "Set outputfile $outputFile"
    if [catch {open $outputFile w} fileId] {
	puts stderr "Cannot open $outputFile : $fileId"
    } else {
	foreach {key value} $parts {
	    #dbg "$key $value"
	    if {[string equal $key "photo"]} {
		
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
	    	dbg "Filename is $fileName"
	    	lappend result filename $fileName
	    }
        }
    }  else {
        # for multipart/*, we'll iterate over children
        # to see if any of them contains the attachment
	# dbg "Children $children"
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
proc send_receipt {receipt src dst} {
    global handle regid DTN_PAYLOAD_MEM COS_NORMAL
    dbg "\nsending geocam return receipt..."
    dbg "$receipt\n\n"
    
#    tell_dtntest 0 dtn_send $h0 regid=$publisher_0 \
     #       source=$g0 dest=$g0 expiration=3600 payload_data="bundle1" \
      #      sequence_id=$seqid01

    set id [dtn_send $handle $regid $dst $src dtn:none $COS_NORMAL 0 86400 $DTN_PAYLOAD_MEM $receipt]
    # puts "bundle id:"
    # puts "  source: [dtn_bundle_id_source_get $id]"
    # puts "  creation_secs: [dtn_bundle_id_creation_secs_get $id]"
    # puts "  creation_seqno: [dtn_bundle_id_creation_seqno_get $id]"
    # delete_dtn_bundle_id $id
}


#----------------------------------------------------------------------
init $argv

while {1} {
    if [catch {
        connect
        register
        proxy_loop
	#debug
    } err] {
        puts "$err"
    }
    catch {dtn_close $handle} err
    after 20000
}