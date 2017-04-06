package com.amap.rctamap;
import android.os.StrictMode;
import android.view.View;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapsInitializer;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.navi.view.AmapCameraOverlay;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.uimanager.LayoutShadowNode;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import java.util.Map;

import javax.annotation.Nullable;

public class AMapViewManager extends ViewGroupManager<AMapView> {

    public static final String REACT_CLASS = "AMap";

    public static final int ANIMATE_TO_REGION = 1;
    public static final int ANIMATE_TO_COORDINATE = 2;
    public static final int FIT_TO_ELEMENTS = 3;
    public static final int ANIMATE_TO_ZOOM_LEVEL = 4;

    private final Map<String, Integer> MAP_TYPES = MapBuilder.of(
            "standard", AMap.MAP_TYPE_NORMAL,
            "satellite", AMap.MAP_TYPE_SATELLITE,
            "night", AMap.MAP_TYPE_NIGHT,//黑夜地图，夜间模式
            "navi", AMap.MAP_TYPE_NAVI//导航模式
    );

    private final Map<String,Integer> MY_LOCATION_TYPE=MapBuilder.of(
            "show",MyLocationStyle.LOCATION_TYPE_SHOW,//只定位
            "mapRotate",MyLocationStyle.LOCATION_TYPE_MAP_ROTATE,//定位、且将视角移动到地图中心点，地图依照设备方向旋转，定位点会跟随设备移动
            "locationRotate",MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE,//定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。
            "typeLocate",MyLocationStyle.LOCATION_TYPE_LOCATE,//定位、且将视角移动到地图中心点。
            "follow",MyLocationStyle.LOCATION_TYPE_FOLLOW//定位、且将视角移动到地图中心点，定位点跟随设备移动。
    );

    private ReactContext reactContext;

    private AMapMarkerManager markerManager;
    private AMapPolylineManager polylineManager;
    private AMapPolygonManager polygonManager;
    private AMapCircleManager circleManager;

    public AMapViewManager(
            AMapMarkerManager markerManager,
            AMapPolylineManager polylineManager,
            AMapPolygonManager polygonManager,
            AMapCircleManager circleManager) {
        this.markerManager = markerManager;
        this.polylineManager = polylineManager;
        this.polygonManager = polygonManager;
        this.circleManager = circleManager;
    }

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    protected AMapView createViewInstance(ThemedReactContext context) {
        reactContext = context;
        AMapView view = new AMapView(context, null);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            MapsInitializer.initialize(context.getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
            emitMapError("AMap initialize error", "amap_init_error");
        }

        return view;
    }

    @Override
    public void onDropViewInstance(AMapView view) {
        view.doDestroy();
        super.onDropViewInstance(view);
    }

    private void emitMapError(String message, String type) {
        WritableMap error = Arguments.createMap();
        error.putString("message", message);
        error.putString("type", type);

        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("onError", error);
    }
    //设置地图类型
    @ReactProp(name = "mapType", defaultInt = AMap.MAP_TYPE_NORMAL)
    public void setMapType(AMapView view, @Nullable String mapType) {
        int typeId = MAP_TYPES.get(mapType);
        view.map.setMapType(typeId);
    }
    //设置中心位置
    @ReactProp(name = "center")
    public void setCenter(AMapView view, ReadableMap center) {
        view.setCenter(center);
    }
    //设置中心点和缩放级别
    @ReactProp(name = "centerZoom")
    public void setCenteZoom(AMapView view, ReadableMap region) {
        view.setCenter(region);
    }
    //设置是否可以缩放
    @ReactProp(name = "zoomEnabled", defaultBoolean = false)
    public void setZoomEnabled(AMapView view, boolean zoomEnabled) {
        view.map.getUiSettings().setZoomGesturesEnabled(zoomEnabled);
        view.map.getUiSettings().setZoomControlsEnabled(zoomEnabled);
    }
    //设置缩放级别
    @ReactProp(name = "zoomLevel", defaultDouble = 10.0)
    public void  setZoomLevel(AMapView view, double zoomLevel) {
        view.setZoomLevel((float) zoomLevel);
    }
    //设置用户定位
    @ReactProp(name = "showsMyLocation")
    public void setShowsMyLocation(AMapView view,  ReadableMap options) {
        String myLocationType=options.getString("myLocationType");
        int interval=options.getInt("interval");
        boolean myLocationEnabled=options.getBoolean("myLocationEnabled");
        MyLocationStyle myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类
        myLocationStyle.myLocationType(MY_LOCATION_TYPE.get(myLocationType));//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
        myLocationStyle.interval(interval); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        //myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.navi_map_gps_locked));
        AMap map=view.getMap();
        map.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        map.getUiSettings().setMyLocationButtonEnabled(myLocationEnabled);//设置默认定位按钮是否显示，非必需设置。
        map.setMyLocationEnabled(myLocationEnabled);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。

    }

    @ReactProp(name = "region")
    public void setRegion(AMapView view, ReadableMap region) {
        view.setRegion(region);
    }




    //设置是否打开交通图层
    @ReactProp(name = "showsTraffic", defaultBoolean = false)
    public void setShowTraffic(AMapView view, boolean showTraffic) {
        view.map.setTrafficEnabled(showTraffic);
    }

//    @ReactProp(name = "showsBuildings", defaultBoolean = false)
//    public void setShowBuildings(AMapView view, boolean showBuildings) {
//        view.map.setBuildingsEnabled(showBuildings);
//    }

    //// TODO: 16/5/19
//    @ReactProp(name = "showsIndoors", defaultBoolean = false)
//    public void setShowIndoors(AMapView view, boolean showIndoors) {
//        view.map.setIndoorEnabled(showIndoors);
//    }
    //设置指南针是否可见
    @ReactProp(name = "showsCompass", defaultBoolean = false)
    public void setShowsCompass(AMapView view, boolean showsCompass) {
        view.map.getUiSettings().setCompassEnabled(showsCompass);
    }
    //设置比例尺控件是否可见
    @ReactProp(name = "showsScale", defaultBoolean = false)
    public void setShowsScale(AMapView view, boolean showsScale) {
        view.map.getUiSettings().setScaleControlsEnabled(showsScale);
    }
    //设置拖拽手势是否可用
    @ReactProp(name = "scrollEnabled", defaultBoolean = false)
    public void setScrollEnabled(AMapView view, boolean scrollEnabled) {
        view.map.getUiSettings().setScrollGesturesEnabled(scrollEnabled);
    }

    //设置旋转手势是否可用
    @ReactProp(name = "rotateEnabled", defaultBoolean = false)
    public void setRotateEnabled(AMapView view, boolean rotateEnabled) {
        view.map.getUiSettings().setRotateGesturesEnabled(rotateEnabled);
    }
    //设置倾斜手势是否可用
    @ReactProp(name = "pitchEnabled", defaultBoolean = false)
    public void setPitchEnabled(AMapView view, boolean pitchEnabled) {
        view.map.getUiSettings().setTiltGesturesEnabled(pitchEnabled);
    }

    @Override
    public void receiveCommand(AMapView view, int commandId, @Nullable ReadableArray args) {
        Integer duration;
        Double lat;
        Double lng;
        Double lngDelta;
        Double latDelta;
        ReadableMap region;
        Double zoomLevel;

        switch (commandId) {
            case ANIMATE_TO_REGION:
                region = args.getMap(0);
                duration = args.getInt(1);
                lng = region.getDouble("longitude");
                lat = region.getDouble("latitude");
                lngDelta = region.hasKey("longitudeDelta")?region.getDouble("longitudeDelta"):0.0;
                latDelta = region.hasKey("latitudeDelta")?region.getDouble("latitudeDelta"):0.0;
                LatLngBounds bounds = new LatLngBounds(
                        new LatLng(lat - latDelta / 2, lng - lngDelta / 2), // southwest
                        new LatLng(lat + latDelta / 2, lng + lngDelta / 2)  // northeast
                );
                view.animateToRegion(bounds, duration);
                break;

            case ANIMATE_TO_COORDINATE:
                region = args.getMap(0);
                duration = args.getInt(1);
                lng = region.getDouble("longitude");
                lat = region.getDouble("latitude");
                view.animateToCoordinate(new LatLng(lat, lng), duration);
                break;

            case ANIMATE_TO_ZOOM_LEVEL:
                zoomLevel = args.getDouble(0);
                view.setZoomLevel(zoomLevel.floatValue());
                break;
            case FIT_TO_ELEMENTS:
                view.fitToElements(args.getBoolean(0));
                break;
        }
    }

    @Override
    @Nullable
    public Map getExportedCustomDirectEventTypeConstants() {
        Map map = MapBuilder.of(
                "onMapReady", MapBuilder.of("registrationName", "onMapReady"),
                "onPress", MapBuilder.of("registrationName", "onPress"),
                "onLongPress", MapBuilder.of("registrationName", "onLongPress"),
                "onMarkerPress", MapBuilder.of("registrationName", "onMarkerPress"),
                "onMarkerSelect", MapBuilder.of("registrationName", "onMarkerSelect"),
                "onMarkerDeselect", MapBuilder.of("registrationName", "onMarkerDeselect"),
                "onCalloutPress", MapBuilder.of("registrationName", "onCalloutPress")
        );

        map.putAll(MapBuilder.of(
                "onMarkerDragStart", MapBuilder.of("registrationName", "onMarkerDragStart"),
                "onMarkerDrag", MapBuilder.of("registrationName", "onMarkerDrag"),
                "onMarkerDragEnd", MapBuilder.of("registrationName", "onMarkerDragEnd")
        ));

        return map;
    }

    @Override
    @Nullable
    public Map<String, Integer> getCommandsMap() {
        return MapBuilder.of(
                "animateToRegion", ANIMATE_TO_REGION,
                "animateToCoordinate", ANIMATE_TO_COORDINATE,
                "fitToElements", FIT_TO_ELEMENTS,
                "animateToZoomLevel", ANIMATE_TO_ZOOM_LEVEL
        );
    }

    @Override
    public LayoutShadowNode createShadowNodeInstance() {
        // A custom shadow node is needed in order to pass back the width/height of the map to the
        // view manager so that it can start applying camera moves with bounds.
        return new SizeReportingShadowNode();
    }

    @Override
    public void addView(AMapView parent, View child, int index) {
        parent.addFeature(child, index);
    }

    @Override
    public int getChildCount(AMapView view) {
        return view.getFeatureCount();
    }

    @Override
    public View getChildAt(AMapView view, int index) {
        return view.getFeatureAt(index);
    }

    @Override
    public void removeViewAt(AMapView parent, int index) {
        parent.removeFeatureAt(index);
    }

    @Override
    public void updateExtraData(AMapView view, Object extraData) {
        view.updateExtraData(extraData);
    }

    public void pushEvent(View view, String name, WritableMap data) {
        ReactContext reactContext = (ReactContext) view.getContext();
        reactContext.getJSModule(RCTEventEmitter.class)
                .receiveEvent(view.getId(), name, data);
    }

}