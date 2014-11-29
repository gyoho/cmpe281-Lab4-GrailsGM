package grailsgumballmachinever2

import gumball.GumballMachine

class GumballStatelessController {

	def String machineSerialNum = "123abc"
	def GumballMachine gumballMachine
	def int salt = 12;
	def timestamp;
	
	def index() {
		
		String VCAP_SERVICES = System.getenv('VCAP_SERVICES')
		
		if (request.method == "GET") {

			// search db for gumball machine
			def gumball = Gumball.findBySerialNumber( machineSerialNum )
			if ( gumball )
			{
				// create a default machine
				gumballMachine = new GumballMachine(gumball.modelNumber, gumball.serialNumber)
				System.out.println(gumballMachine.getAbout())
			}
			else
			{
				flash.message = "Error! Gumball Machine Not Found!"
				render(view: "index")
			}

			// don't save in the session
			// session.machine = gumballMachine
			
			// send machine state to client (instead)
			flash.state = gumballMachine.getCurrentState() ;
			flash.model = gumball.modelNumber ;
			flash.serial = gumball.serialNumber ;
			
			// include extra value for encryption
			flash.timestamp = new Date();
			flash.salt = salt;
			
			// report a message to user
			flash.message = gumballMachine.getAbout();

			// display view
			render(view: "index")

		}
		else if (request.method == "POST") {

			// dump out request object
			request.each { key, value ->
				println( "request: ${key} = ${value}")
			}

			// dump out params
			params?.each { key, value ->
				println( "params: $key = $value" )
			}
			
			
			// don't get machine from session
			// gumballMachine = session.machine
			
			/** check the data integrity by 
			 *  regenerate the hash and match
			 *  against the original
			 **/
			// get each parameter
			def state = params?.state
			def modelNum = params?.model
			def serialNum = params?.serial
			def timestamp = params?.timestamp
			def salt = params?.salt
			def originalHash = params?.hash
			
			println "original hash val: ${originalHash}"

			// generate the hash value
			def newHash = (state + modelNum + serialNum + timestamp + salt).encodeAsSHA256()
			
			println "new hash val: ${newHash}"
			
			if(originalHash == newHash) {

				// restore machine to client state (instead)
				gumballMachine = new GumballMachine(modelNum, serialNum) ;
				gumballMachine.setCurrentState(state) ;
				
				System.out.println(gumballMachine.getAbout())
				
				if ( params?.event == "Insert Quarter" )
				{
					gumballMachine.insertCoin()
				}
				if ( params?.event == "Turn Crank" )
				{
					gumballMachine.crankHandle();
					
					if ( gumballMachine.getCurrentState().equals("gumball.CoinAcceptedState") )
					{
						def gumball = Gumball.findBySerialNumber( machineSerialNum )
						if ( gumball )
						{						
							// gumball.lock() // pessimistic lock
							if ( gumball.countGumballs > 0)
								gumball.countGumballs-- ;
							gumball.save(flush: true); // default optimistic lock
						}
					}
					
				}
	
				// send machine state to client
				flash.state = gumballMachine.getCurrentState() ;
				flash.model = modelNum ;
				flash.serial = serialNum ;
				
				// include extra value for encryption
				flash.timestamp = new Date();
				flash.salt = salt;
							
				// report a message to user
				flash.message = gumballMachine.getAbout()
	
				// render view
				render(view: "index")
			}
			else {
				render(view: "/error")
			}
		}
		else {
			render(view: "/error")
		}
	}

}

