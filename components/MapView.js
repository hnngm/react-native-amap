import React,{PropTypes} from 'react';
import {
  EdgeInsetsPropType,
  NativeMethodsMixin,
  Platform,
  ReactNativeViewAttributes,
  View,
  Animated,
  requireNativeComponent,
  NativeModules,
} from 'react-native';


import MapMarker from './MapMarker';
import MapPolyline from './MapPolyline';
import MapPolygon from './MapPolygon';
import MapCircle from './MapCircle';
import MapCallout from './MapCallout';
import MapLocation from './MapLocation';

class MapView extends React.Component {
    static defaultProps = {
        zoomLevel: 13,
        region:null
    }; 
    static viewConfig={
        uiViewClassName: 'AMap',
        validAttributes: {
          region: true,
        },
      };

    static propTypes={
            ...View.propTypes,

            style: View.propTypes.style,

            /**
             * [apiKey amap's apiKey]
             * @type {[type]}
             */
            apiKey: PropTypes.string,
            

            /**
            delete
             * If `false` points of interest won't be displayed on the map.
             * Default value is `true`.
             *
             */
            showsPointsOfInterest: PropTypes.bool,

            /**
            delete
             * A Boolean indicating whether the map displays extruded building information.
             * Default value is `true`.
             */
            showsBuildings: PropTypes.bool,

            

            /**
            delete
             * A Boolean indicating whether indoor maps should be enabled.
             * Default value is `false`
             *
             * @platform android
             */
            showsIndoors: PropTypes.bool,

            
            //delete
            userTrackingMode: PropTypes.oneOf([
                'none',
                'follow',
                'followWithHeading',
            ]),

            /**
             * The region to be displayed by the map.
             *
             * The region is defined by the center coordinates and the span of
             * coordinates to display.
             */
            region: PropTypes.shape({
                /**
                 * Coordinates for the center of the map.
                 */
                latitude: PropTypes.number.isRequired,
                longitude: PropTypes.number.isRequired,

                /**
                 * Difference between the minimun and the maximum latitude/longitude
                 * to be displayed.
                 */
                latitudeDelta: PropTypes.number,
                longitudeDelta: PropTypes.number,
            }),

            /**
            delete
             * The initial region to be displayed by the map.  Use this prop instead of `region`
             * only if you don't want to control the viewport of the map besides the initial region.
             *
             * Changing this prop after the component has mounted will not result in a region change.
             *
             * This is similar to the `initialValue` prop of a text input.
             */
            initialRegion: PropTypes.shape({
                /**
                 * Coordinates for the center of the map.
                 */
                latitude: PropTypes.number.isRequired,
                longitude: PropTypes.number.isRequired,

                /**
                 * Difference between the minimun and the maximum latitude/longitude
                 * to be displayed.
                 */
                latitudeDelta: PropTypes.number,
                longitudeDelta: PropTypes.number,
            }),
            //设置地图类型
            mapType: PropTypes.oneOf([
                'standard',
                'satellite',
                'night',
                'navi',
            ]),
            //设置中心位置
            center: PropTypes.shape({
                latitude: PropTypes.number.isRequired,
                longitude: PropTypes.number.isRequired,
            }),
            //设置中心和缩放级别
            centerZoom:PropTypes.shape({
                latitude: PropTypes.number.isRequired,
                longitude: PropTypes.number.isRequired,
                zoom:PropTypes.number.isRequired,
            }),
            //设置显示用户定位
            showsMyLocation: PropTypes.shape({
                myLocationType: PropTypes.oneOf([
                                                  'show',
                                                  'mapRotate',
                                                  'locationRotate',
                                                  'typeLocate',
                                                  'follow'
                                              ]),
                interval: PropTypes.number.isRequired,
                myLocationEnabled:PropTypes.bool,
            }),
            //设置指南针是否可见
            showsCompass: PropTypes.bool,
            //设置是否关闭缩放
            zoomEnabled: PropTypes.bool,
            //设置缩放级别
            zoomLevel: PropTypes.number,
            //设置旋转手势是否可用
            rotateEnabled: PropTypes.bool,
            //设置拖拽手势是否可用
            scrollEnabled: PropTypes.bool,
            //设置倾斜手势是否可用
            pitchEnabled: PropTypes.bool,
            //设置比例尺控件是否可见
            showsScale: PropTypes.bool,
            //设置是否打开交通图层
            showsTraffic: PropTypes.bool,

            /**
            delete
             * Maximum size of area that can be displayed.
             *
             * @platform ios
             */
            maxDelta: PropTypes.number,

            /**
            delete
             * Minimum size of area that can be displayed.
             *
             * @platform ios
             */
            minDelta: PropTypes.number,

            /**
             * Insets for the map's legal label, originally at bottom left of the map.
             * See `EdgeInsetsPropType.js` for more information.
             */
            legalLabelInsets: EdgeInsetsPropType,

            /**
             * Callback that is called continuously when the user is dragging the map.
             */
            onRegionChange: PropTypes.func,

            /**
             * Callback that is called once, when the user is done moving the map.
             */
            onRegionChangeComplete: PropTypes.func,

            /**
             * Callback that is called when user taps on the map.
             */
            onPress: PropTypes.func,

            /**
             * Callback that is called when user makes a "long press" somewhere on the map.
             */
            onLongPress: PropTypes.func,

            /**
             * Callback that is called when a marker on the map is tapped by the user.
             */
            onMarkerPress: PropTypes.func,

            /**
             * Callback that is called when a marker on the map becomes selected. This will be called when
             * the callout for that marker is about to be shown.
             *
             * @platform ios
             */
            onMarkerSelect: PropTypes.func,

            /**
             * Callback that is called when a marker on the map becomes deselected. This will be called when
             * the callout for that marker is about to be hidden.
             *
             * @platform ios
             */
            onMarkerDeselect: PropTypes.func,

            /**
             * Callback that is called when a callout is tapped by the user.
             */
            onCalloutPress: PropTypes.func,

            /**
             * Callback that is called when the user initiates a drag on a marker (if it is draggable)
             */
            onMarkerDragStart: PropTypes.func,

            /**
             * Callback called continuously as a marker is dragged
             */
            onMarkerDrag: PropTypes.func,

            /**
             * Callback that is called when a drag on a marker finishes. This is usually the point you
             * will want to setState on the marker's coordinate again
             */
            onMarkerDragEnd: PropTypes.func,
        };
    constructor(props) {
      super(props);
    
      this.state = {
          isReady: false,
      };
    }

    //组件加载完毕之后立即执行
  componentDidMount() {
      const {
          region,
          initialRegion,
          showsScale
      } = this.props;
      if (region && this.state.isReady) {
          this.refs.map.setNativeProps({
              region
          });
      } else if (initialRegion && this.state.isReady) {
          this.refs.map.setNativeProps({
              region: initialRegion
          });
      }
  };
  //在组件接收到新的props或者state但还没有render时被执行,在初始化时不会被执行
  componentWillUpdate(nextProps) {
      var a = this.__lastRegion;
      var b = nextProps.region;
      if (!a || !b) return;
      if (
          a.latitude !== b.latitude ||
          a.longitude !== b.longitude ||
          a.latitudeDelta !== b.latitudeDelta ||
          a.longitudeDelta !== b.longitudeDelta
      ) {
          this.refs.map.setNativeProps({
              region: b
          });
      }
  };
  //地图启动成功调用
  _onMapReady() {
      //console.log("_onMapReady"+this.state.isReady);
      const {
          region,
          initialRegion
      } = this.props;
      if (typeof this.props.onMapReady == 'function') {
          this.props.onMapReady()
      }
      if (region) {
          this.refs.map.setNativeProps({
              region
          });
      } else if (initialRegion) {
          this.refs.map.setNativeProps({
              region: initialRegion
          });
      }
      
      this.setState({isReady: true});
  };
  //当挂载或者布局变化以后调
  _onLayout(e) {
    
      const {
          region,
          initialRegion,
          onLayout
      } = this.props;
      const {
          isReady
      } = this.state;
      if (region && isReady && !this.__layoutCalled) {
          this.__layoutCalled = true;
          this.refs.map.setNativeProps({
              region
          });
      } else if (initialRegion && isReady && !this.__layoutCalled) {
          this.__layoutCalled = true;
          this.refs.map.setNativeProps({
              region: initialRegion
          });
      }
      onLayout && onLayout(e);
  };
  //地图改变时触发
  _onChange(event: Event) {
    console.log("_onChange");
      this.__lastRegion = event.nativeEvent.region;
      if (event.nativeEvent.continuous) {
          this.props.onRegionChange &&
              this.props.onRegionChange(event.nativeEvent.region);
      } else {
          this.props.onRegionChangeComplete &&
              this.props.onRegionChangeComplete(event.nativeEvent.region);
      }
  };

  animateToRegion(region, duration) {
      this._runCommand('animateToRegion', [region, duration || 500]);
  };

  animateToCoordinate(latLng, duration) {
      this._runCommand('animateToCoordinate', [latLng, duration || 500]);
  };

  fitToElements(animated) {
      this._runCommand('fitToElements', [animated]);
  };

  animateToZoomLevel(zoomLevel) {
      this._runCommand('animateToZoomLevel', [zoomLevel])
  };

  _getHandle() {
      return ReactNative.findNodeHandle(this.refs.map);
  };

  _runCommand(name, args) {
      switch (Platform.OS) {
          case 'android':
              NativeModules.UIManager.dispatchViewManagerCommand(
                  this._getHandle(),
                  NativeModules.UIManager.AMap.Commands[name],
                  args
              );
              break;

          case 'ios':
              NativeModules.AMapManager[name].apply(
                  NativeModules.AMapManager[name], [this._getHandle(), ...args]
              );
              break;
      }
  };
  

  render() {
      let props = {
          ...this.props,
          region: null,
          initialRegion: null,
      };
      if (Platform.OS === 'ios' && props.mapType === 'terrain') {
          props.mapType = 'standard';
      }
      if (!props.style || !props.style.flex) {
          props.style = props.style || {}
          props.style.flex = 1
      }

      return ( < AMap ref = "map" {...props} 
        onLayout={e=>this._onLayout(e)}
        onMapReady={e=>this._onMapReady(e)}
        onChange={e=>this._onChange(e)}
        />);
  }

}

var AMap = requireNativeComponent('AMap', MapView, {
  nativeOnly: {
    onChange: true,
    onMapReady: true,
  },
});

MapView.Marker = MapMarker;
MapView.Polyline = MapPolyline;
MapView.Polygon = MapPolygon;
MapView.Circle = MapCircle;
MapView.Callout = MapCallout;
MapView.MapLocation=MapLocation;

MapView.Animated = Animated.createAnimatedComponent(MapView);

module.exports = MapView;
