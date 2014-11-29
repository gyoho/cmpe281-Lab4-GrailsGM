package grailsgumballmachinever2

// expose a domain class as a REST resource
import grails.rest.*
@Resource(uri='/gumballs',formats=['json'])

class Gumball {

    String modelNumber
    String serialNumber
    Integer countGumballs

    static constraints = {
        serialNumber(unique: true)
    }
}
