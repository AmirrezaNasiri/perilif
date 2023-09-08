void log(const char* message) {
  if (DEBUG) {
    Serial.println(message);
  }
}

void log(const __FlashStringHelper* message) {
  if (DEBUG) {
    Serial.println(message);
  }
}

void sendJson(const JsonDocument& doc) {
  if (DEBUG) {
    log(F("[i] sent to bluetooth:"));
    serializeJsonPretty(doc, Serial);
    Serial.println();
  }
  
  serializeJson(doc, bluetooth);
  bluetooth.println();
}

void sendSignal(const __FlashStringHelper* type) {
  StaticJsonDocument<32> doc;
  doc[F("_t")] = type;
  sendJson(doc);
}

void beginCurrentMeasuring() {
  lastCurrent = getCurrent();
}

float measureCurrent() {
  float value = getCurrent() - lastCurrent;
  return value > 0 ? value : 0;
}

float getCurrent() {
  delay(10);
  float value = current.mA_DC();
  return value > 0 ? value : 0;
}

void adjustPad(int pin, int value) {
  analogWrite(pin, (int) (value * 255 / 100));
}
