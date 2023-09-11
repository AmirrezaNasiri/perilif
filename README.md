# Perilif: Period Pain Reliever 🌸

<p align="center">
  <img src="assets/perilif.jpg" alt="Perilif Image" width="465" />
</p>


Welcome to Perilif! This is a delightful, heat pad-based buddy designed to soothe menstrual cramps and IBS pain. Built with love on Arduino, it's paired with a cute Android app for easy control.

**Now, a little secret 🤫:** Perilif isn't meant for big-scale, real-life production. Think of it more like a DIY bestie for those curious minds out there. Sure, there are many high-tech gadgets you can buy, but if you fancy crafting something unique for yourself or a loved one, this is a fabulous place to start. 

## Stack 🛠️
Dive into the stack behind Perilif:

### Software

- **Kotlin** 📱:
  We've whipped up a nifty Android app using Kotlin. Connect and control your Perilif right from your phone, all thanks to the Bluetooth protocol.

  You can control each of the pads individually or turn the Turbo feature on to maximize the heat for a short period.

  The source code is available in the [android-app (kotlin)](android-app (kotlin)) directory.

<p align="center">
  <img src="assets/app.png" width="800" />
</p>


### Hardware
#### Components 🤖
- **Arduino Nano:**

  Using the nimble Arduino Nano, it’s the brain of our little device. It chirps via Bluetooth, keeps an eye on things, and makes sure the heat pads are just the right kind of cozy. PWM signals are the way it controls the heat level.

  The C++ source code of the Arduino project is available in [arduino-hardware (cpp)](arduino-hardware (cpp)) directory which can be opened inside Arduino IDE.

<p align="center">
  <img src="assets/arduino-nano-v3.jpg" width="300" />
</p>


- **HC-05 Bluetooth Module:**

  An HC-05 Bluetooth module (with the helper board) is used for communicating with the Android app.

<p align="center">
  <img src="assets/hc-05.jpg" width="300" />
</p>

- **Current Sensor:**

  A current sensor (ACS712) is used to gather initial power-source data and calibrate the application using that.

<p align="center">
  <img src="assets/current-sensor.jpg" width="300" />
</p>

- **Power Supply (Adapter):**
  
  We used a reliable power supply that powers the device with 5V DC and 6A (max). 6A is required since we used 3 pads with 2A consumption.

  Since the device is not efficient with managing PWM signals, it can draw the total amount of current required by the heating pads. So with the current inefficient implementation, you need a reliable power supply.

<p align="center">
  <img src="assets/power-supply.jpg" width="300"/>
</p>

- **MOSFETs:**

  For amplification, three MOSFETs (IRF3710) are responsible for controlling the current consumption of the heating pads by opening or closing their gate according to the received PWM signal.

  <p align="center">
    <img src="assets/irf3710.jpg" width="300" />
  </p>

- **Heating Pads:**

  3 heating pads are used with the specification of a 3.7-5V operation range and 2A current consumption for each.
  
  <p align="center">
    <img src="assets/heating-pad.jpg" width="300" />
  </p>
  
  The heating pads are wrapped inside a fireproof fiber to reduce the contactable heat, then wrapped inside a bunch of Kinesio Elastic Therapeutic tapes to make them flexible and less annoying.

  <p align="center">
    <img src="assets/layers.jpg" width="500" />
  </p>


- **Case:**
  A combination of MOSFETs is also responsible for amplifying the PWM signals produced by the microcontroller to the heating pads.

  Here is an inner view of the plastic case:

  <p align="center">
    <img src="assets/inner.jpg" width="500" />
  </p>

- **Back Brace:**

  A comfortable back brace to keep the heating pads in the place you need.

  <p align="center">
    <img src="assets/back-brace.jpg" width="300" />
  </p>

- **Wires, connectors, and soldering:**

  Heating pads require a high amount of current which requires proper and standard wires. Make sure that your wires can pass the required current without warming up (they do! so double-check the specifications).

  Same as the wires, all of your connectors must be able to pass the current too.

  If you're soldering on board, make sure the parts that are responsible for passing high currents are wide enough.


- **Other:**

  Just like any other project, we need a bunch of resistors (as described in the schematic), wire protectors, tapes, board, soldering iron, etc.

#### Schematic 🔧
  All those wires and thingamajigs? We've sketched them out using [Fritzing](https://fritzing.org/) (an open-source hardware initiative software). It's like a digital canvas, simple and fun to play around with.

  The fritzing source file is available in the [schematic (fritzing)](schematic (fritzing)) directory. Here is the schematic. Note that those <img src="assets/heating-pad-symbol.png" alt="Perilif Image" width="20" height="20" /> indicate the heating pads.

<p align="center">
  <img src="schematic (fritzing)/schematic.png" alt="Perilif Image" width="465"  />
</p>

## ⚠️ Important Notes and Known Issues
If you're going to build the device, use it, or contribute to improving it, consider the following notes very carefully:

### Bluetooth communication is not stable
Sometimes, you need to restart the device and/or the application to make Bluetooth communication possible. 

### Device must not be exposed to the body
Although the device is supplied with 5v (which is considered low), it can be still considered as not safe and must NOT be exposed to the body. Be very careful when building the device to make it less probable to make raw contact with the body.

Take note that the heat pads, tapes, fireproof fibers, casing, wiring, and other components used in this project must not be considered waterproof and must not be exposed directly to the body.

Direct contact with the body will lead to a small shock. For non-dry skin or open wounds, it's considered dangerous.

### Take care of fire hazard
Most parts of the device have the potential to get extremely warm. The heat pads, device case, components, the board inside, all of the wires, and the power supply can get heated very quickly.

Other than the PWM signals, NO mechanism is implemented to make the device safe, especially from warming up. If something gets hot, disconnect the device - wires are very potential.

### PWM signals are not implemented efficiently
Arduino uses PWM signals to order whether the heating pads should be off or not. In a control cycle, it may order all of the gates to open which will lead to high pressure on the power source.

In very rare cases it's needed to use all the power provided by the power source, but with the current design, it does happen a lot of times.

This way of implementation puts more unnecessary pressure on the power source which may lead to noise generation and reduction of the power-supply life.

### Amplifiers are not implemented efficiently
There are very considerable amount of energy wasted inside the device (in MOSFETs), instead of efficiently using it.

 This implementation leads to more-than-needed energy consumption to heat up the pads, heating the case and inner components, and reducing the device life.

## ⚠️ Strict Disclaimer

Before attempting to use or recreate the Perilif device or software, please read and understand the following disclaimer:

1. **Potential Injuries**: The Perilif device is a DIY project and, as such, has not undergone rigorous safety testing. Incorrect use or malfunctions can result in burns, electrical shocks, or other injuries. Always supervise the device while in use and NEVER sleep while it's operating.

2. **Fire Hazard**: The components, especially the heating pads, can become extremely hot and pose a risk of fire if they come into contact with flammable materials or malfunction. Ensure you're using the device in a safe environment.

3. **Electrical Safety**: While the device operates at 5V, which is considered low, it can still cause an electric shock or do injure, especially when in direct contact with the skin or if the skin is wet. Never expose any component of the device directly to your body.

4. **Medical Concerns**: If you have underlying medical conditions, consult with a healthcare professional before using the device. Continuous exposure to heat can exacerbate certain health issues.

5. **Liability**: The creators and contributors of Perilif will not be held responsible for any injuries, damages, or issues resulting from the use or misuse of the device or software. By choosing to use or recreate the Perilif, you acknowledge and accept all risks associated with it.

Always prioritize safety above all else and cease the use of the device at the first sign of malfunction or concern. If in doubt, please refrain from using it altogether.


Thanks for stopping by! Happy crafting and stay comfy! 😊❤️