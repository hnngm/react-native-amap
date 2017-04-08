import {NativeModules, DeviceEventEmitter} from 'react-native';

const MapLocation = NativeModules.AMapLocation;
const onLocationChanged = 'onLocationChangedAMAPLOCATION';


export default class ALocation {

  static startLocation(options) {
    MapLocation.startLocation(options);
  }

  static stopLocation() {
    MapLocation.stopLocation();
  }

  static addEventListener(handler) {

    const listener = DeviceEventEmitter.addListener(
        onLocationChanged,
        handler,
    );
    return listener;
  }
}