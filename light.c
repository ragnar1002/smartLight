#include <stdio.h>
#include <stdlib.h>
#include "coap-log.h"
#include "sys/log.h"
#include "sys/node-id.h"
#include "contiki.h"
#include "coap-engine.h"
#include "coap-blocking-api.h"
#include "sys/etimer.h"
#include "os/dev/leds.h"

#define LOG_MODULE "NODE"
#define LOG_LEVEL LOG_LEVEL_DBG

#define PRESENCE_THRESHOLD 50



//resource definition
extern coap_resource_t res_presence; 
extern coap_resource_t res_light; 

extern bool lightOn;
extern int presence;
bool oldLightOn;
bool registered = false;

#define SERVER_EP "coap://[fd00::1]:5683"



PROCESS(node_process, "Sensor node");
AUTOSTART_PROCESSES(&node_process);

static struct etimer timer;


int get_presence(){
	int p =(1 + rand()%100);
	return p;
}
void change_leds_status(int presence){
	if (presence < PRESENCE_THRESHOLD){ //not present
			leds_set(LEDS_NUM_TO_MASK(LEDS_RED));
	}
	else if (presence >= PRESENCE_THRESHOLD){
			leds_set(LEDS_NUM_TO_MASK(LEDS_GREEN));
	}
}
//for the coap_blocking_request
void client_chunk_handler(coap_message_t *response)
{

	const uint8_t *chunk;
	if (response == NULL){
		LOG_INFO("request time out\n");
		return;
	}
	int len = coap_get_payload(response, &chunk);
	registered = true;

	LOG_INFO("|%.*s \n", len, (char *)chunk);


}

PROCESS_THREAD(node_process, ev, data){

	static coap_endpoint_t server_ep;
	static coap_message_t request[1];

	PROCESS_BEGIN();
	

	leds_set(LEDS_NUM_TO_MASK(LEDS_RED)); // at the beginning, all lights are off

	coap_activate_resource(&res_presence, "res_presence");
	coap_activate_resource(&res_light, "res_light");

	coap_endpoint_parse(SERVER_EP, strlen(SERVER_EP), &server_ep);

	coap_init_message(request, COAP_TYPE_CON, COAP_GET ,0);
	coap_set_header_uri_path(request, "registration");
	

	char msg[4];
	
	sprintf(msg,"%d",node_id);
	coap_set_payload(request, (uint16_t * )msg, sizeof(msg)-1);


	while(!registered){
		LOG_DBG("Retrying registration..\n");
		COAP_BLOCKING_REQUEST(&server_ep, request, client_chunk_handler);
	}

	LOG_DBG("Registered\n");


	etimer_set(&timer,30 * CLOCK_SECOND);
	
	while(1){
		
		
		PROCESS_WAIT_EVENT_UNTIL(etimer_expired(&timer));
		oldLightOn = lightOn;
		
		presence = get_presence();
		
		if(presence >= PRESENCE_THRESHOLD){
			LOG_DBG("Presence detected!\n");
		}
		else{
			LOG_DBG("Presence NOT detected!\n");
		}
		
		
		
		lightOn = (presence >= PRESENCE_THRESHOLD ? 1 : 0);
		if(lightOn != oldLightOn){ // when state changes, trigger call
			res_presence.trigger();
		}
		
		change_leds_status(presence);
		etimer_reset(&timer);

	}
	
	PROCESS_END();
}



