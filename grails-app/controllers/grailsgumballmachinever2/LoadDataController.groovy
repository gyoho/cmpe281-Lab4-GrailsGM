package grailsgumballmachinever2

class LoadDataController {

	def index() {
		// flush: true is necessary
		new Gumball(modelNumber: "7", serialNumber: "123abc", countGumballs: 5).save(flush: true)

		render "done!"
	}
}
