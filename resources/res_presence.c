#include <stdio.h>
#include <stdlib.h>
#include "contiki.h"
#include "coap-engine.h"
#include "coap-observe.h"
#include "os/dev/leds.h"
#include "sys/etimer.h"

#include "sys/log.h"
#define LOG_MODULE "light sensor"
#define LOG_LEVEL LOG_LEVEL_DBG

#define PRESENCE_THRESHOLD 50

static int counter = 0;
extern bool lightOn; 
int presence = 20;



static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);

static void res_event_handler(void);


EVENT_RESOURCE(res_presence,
	"title=\"Presence sensor\";methods=\"GET\";rt=\"int\";obs\n",
	res_get_handler,
	NULL,
        NULL,
	NULL,
	res_event_handler);


static void res_event_handler(void){
	counter++;
	coap_notify_observers(&res_presence);
}
static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset){


	if(request != NULL){
		LOG_DBG("Observing handler number %d\n", counter); 
	}


	unsigned int accept = -1;
	coap_get_header_accept(request, &accept);


	if (accept== -1)
		accept = APPLICATION_JSON;

	if(accept == APPLICATION_XML) {
		coap_set_header_content_format(response, APPLICATION_XML);
 		snprintf((char *)buffer, COAP_MAX_CHUNK_SIZE, "<presence=\"%d\"/>", presence);
		coap_set_payload(response, buffer, strlen((char *)buffer));
    	} 
	else if(accept == APPLICATION_JSON) {
		coap_set_header_content_format(response, APPLICATION_JSON);
		snprintf((char *)buffer, COAP_MAX_CHUNK_SIZE, "{\"presence\":%d}", presence);
		coap_set_payload(response, buffer, strlen((char *)buffer));
	}
	else {
		coap_set_status_code(response, NOT_ACCEPTABLE_4_06);
		const char *msg = "Supporting content-type application/json";
		coap_set_payload(response, msg, strlen(msg));
  	}

}





