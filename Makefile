CONTIKI_PROJECT = device
all: $(CONTIKI_PROJECT)
CFLAGS += -DPROJECT_CONF_H=\"project-conf.h\"
CONTIKI = ../../..

# Include optional target-specific module
include $(CONTIKI)/Makefile.identify-target
MODULES_REL += $(TARGET)

# Include CoAP resources
MODULES_REL += ./resources

# Include the CoAP implementation
include $(CONTIKI)/Makefile.dir-variables
MODULES += $(CONTIKI_NG_APP_LAYER_DIR)/coap

include $(CONTIKI)/Makefile.include
