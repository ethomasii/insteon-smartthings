/**
 *  Insteon switch
 *
 *  Author: ethomasii@gmail.com
 *
 *  Date: 2013-12-08
 */
preferences {
    input("deviceid", "text", title: "Device ID", description: "Your Insteon device.  Do not include periods.")
    input("host", "text", title: "URL", description: "The URL of your SmartLinc")
    input("port", "text", title: "Port", description: "The port")
} 
 
metadata {
	// Automatically generated. Make future change here.
	definition (name: "Insteon", author: "ethomasii@gmail.com", oauth: true) {
		capability "Polling"
        capability "Switch"
	}

	// simulator metadata
	simulator {
	}

	// UI tile definitions
	tiles {
		standardTile("button", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "off", label: 'Off', action: "switch.on", icon: "st.switches.light.off", backgroundColor: "#ffffff", nextState: "on"
			state "on", label: 'On', action: "switch.off", icon: "st.switches.light.on", backgroundColor: "#79b821", nextState: "off"
		}
		main "button"
		details "button"
	}
}

def parse(String description) {
}

def on() {
	log.debug "we made it"
	sendCmd("11", "FF")
	sendEvent(name: "switch", value: "on");
}

def off() {
	log.debug "we off it"
	sendCmd("13", "00")
	sendEvent(name: "switch", value: "off");
}

def sendCmd(num, level)
{
	log.debug "in sendcmd"
	/* http://${settings.host}:${settings.port}/3?0262${settings.deviceid}0F${num}FF=I=3 */
    httpGet("http://${settings.host}:${settings.port}//3?0262${settings.deviceid}0F${num}${level}=I=3") {response -> 
        def content = response.data
        log.debug content
    }
    log.debug "after our get"
}

def poll()
{
	sendCmd("19", "00")
	getStatus()
}

def initialize(){
	def freq = 1
	schedule("0 0/$freq * * * ?", checkLight)
}

def checkLight(){
    poll()
}

def getStatus() {
    httpGet("http://${settings.host}:${settings.port}/buffstatus.xml") {response -> 
        def content = response.data
        log.debug content
        
        if(content.text().length() == 40)
        {
        	log.debug content.text().substring(22,28)
			if(content.text().substring(22,28) == '208E36')
            {
				log.debug content.text().substring(38,40)
                if(content.text().substring(38,40) == '00' || content.text().substring(38,40) == '01')
                {
                    log.debug "turn it off"
                    sendEvent(name: "switch", value: "off");
                }
                else
                {
                    log.debug "turn it on"
                    sendEvent(name: "switch", value: "on");
                }
            }

			else
            {
            	sendCmd("19", "00")
            	getStatus()
            }
        }
        else
        {
			sendCmd("19", "00")        
        	getStatus()
        }
   }
}
