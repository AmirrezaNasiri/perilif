#include <NeoSWSerial.h>
#include <ACS712.h>
#include <ArduinoJson.h>
#include <StreamUtils.h>

#define DEBUG false
#define BLUETOOTH_RX_PIN 2
#define BLUETOOTH_TX_PIN 3
#define BLUETOOTH_STATE_PIN 4
#define CURRENT_SENSOR_PIN A0
#define CURRENT_SENSOR_SENSITIVITY 66
#define PAD_A_PWM_PIN 9
#define PAD_B_PWM_PIN 10
#define PAD_C_PWM_PIN 11

NeoSWSerial bluetooth(BLUETOOTH_RX_PIN, BLUETOOTH_TX_PIN);
ACS712 current(CURRENT_SENSOR_PIN, 5.0, 1023, CURRENT_SENSOR_SENSITIVITY);

float lastCurrent;
int padA_Cycle, padB_Cycle, padC_Cycle = 0;

void setup() {
  if (DEBUG) {
    Serial.begin(74880);
    while (!Serial) continue;
  }

  current.autoMidPoint();
  
  pinMode(PAD_A_PWM_PIN, OUTPUT);
  pinMode(PAD_B_PWM_PIN, OUTPUT);
  pinMode(PAD_C_PWM_PIN, OUTPUT);

  log(F("[*] waiting for bluetooth connection ..."));

  pinMode(BLUETOOTH_STATE_PIN, INPUT);
  bluetooth.begin(9600);
  while (digitalRead(BLUETOOTH_STATE_PIN) == LOW) continue;

  sendSignal(F("ping"));
  sendInitialReport();
  delay(1000);
}

void loop() {
  processIncomingCommand();
  sendPeriodicReport();

  adjustPad(PAD_A_PWM_PIN, padA_Cycle);
  adjustPad(PAD_B_PWM_PIN, padB_Cycle);
  adjustPad(PAD_C_PWM_PIN, padC_Cycle);

  delay(1000);
}

void sendInitialReport() {
  log(F("[i] initial report:"));

  beginCurrentMeasuring();
  
  StaticJsonDocument<96> json;

  json[F("_t")] = F("ir");

  // Calculate max current of each pad
  adjustPad(PAD_A_PWM_PIN, 100);
  json[F("pamc")] = measureCurrent();
  adjustPad(PAD_A_PWM_PIN, 0);

  adjustPad(PAD_B_PWM_PIN, 100);
  json[F("pbmc")] = measureCurrent();
  adjustPad(PAD_B_PWM_PIN, 0);

  adjustPad(PAD_C_PWM_PIN, 100);
  json[F("pcmc")] = measureCurrent();
  adjustPad(PAD_C_PWM_PIN, 0);

  adjustPad(PAD_A_PWM_PIN, 100);
  adjustPad(PAD_B_PWM_PIN, 100);
  adjustPad(PAD_C_PWM_PIN, 100);
  delay(1000);
  json[F("pmc")] = measureCurrent();
  adjustPad(PAD_A_PWM_PIN, 0);
  adjustPad(PAD_B_PWM_PIN, 0);
  adjustPad(PAD_C_PWM_PIN, 0);

  sendJson(json);
}

void sendPeriodicReport() {
  log(F("[i] periodic report:"));

  StaticJsonDocument<192> json;

  json[F("_t")] = F("pr");
  json[F("c")] = getCurrent();
  json[F("pac")] = padA_Cycle;
  json[F("pbc")] = padB_Cycle;
  json[F("pcc")] = padC_Cycle;
  sendJson(json);
}

void processIncomingCommand() {
  if (bluetooth.available()) {
    log(F("[i] command received: "));
    
    StaticJsonDocument<64> json;

    #ifdef DEBUG
      ReadLoggingStream loggingStream(bluetooth, Serial);
      ReadBufferingStream bufferingStream(loggingStream, 64);
    #else
      ReadBufferingStream bufferingStream(bluetooth, 64);
    #endif

    DeserializationError error = deserializeJson(json, bufferingStream);
    
    if (error) {
      log(F("[-] error parsing json:"));
      log(error.f_str());
      sendSignal(F("cmd_err"));
      return;
    }
    
    if (json[F("_t")] == F("c")) {
      padA_Cycle = json[F("pac")].as<int>();
      padB_Cycle = json[F("pbc")].as<int>();
      padC_Cycle = json[F("pcc")].as<int>();
      sendSignal(F("cmd_ok"));
    } else {
      sendSignal(F("cmd_no"));
    }    
  }
}
