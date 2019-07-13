<template>
  <div id="app">
    <div class="container">

      
      

      <div v-if="smartphoneConneted">
        <div class="jumbotron">

          <img src="@/assets/logo.jpg" style="width:400px;padding:20px"/>

          <div v-if="bluetoothConneted">
            <div class="container">
              <div class="row">
                <div class="col-sm">
                  <h1 class="display-4">Bluetooth</h1>
                </div>
                <div class="col-sm">
                  <button type="button" class="btn btn-danger" v-on:click="enableBluetooth(false)">DISCONNECT</button>
                </div>
              </div>
              <hr class="my-4">

              <div class="form-group form-check">
    <input type="checkbox" class="form-check-input" id="exampleCheck1" v-model="enableVib" v-on:change="changeCheckbox">
    <label class="form-check-label" for="exampleCheck1">activate</label>
  </div>

              <vue-slider v-if="enableVib" v-model="sliderVal" v-on:change="changeSlider" />
              <h2 v-if="enableVib">{{sliderVal}}</h2>
            </div>
          </div>
          <div v-else>
            <div class="row">
                <div class="col-sm">
                  <h1 class="display-4">Bluetooth</h1>
                </div>
                <div class="col-sm">
                  <button type="button" class="btn btn-success" v-on:click="enableBluetooth(true)">Connect</button>
                </div>
              </div>
          </div>

          

        </div>
      </div>
      <h1 v-else>Smartphone is not connected :(x</h1>

   
    </div>
  </div>
</template>

<script>
import VueSlider from 'vue-slider-component'

export default {
  name: 'app',
  components: {
    VueSlider
  },
  data: function(){
    return {
      smartphoneConneted: false,
      bluetoothConneted: false,
      sliderVal: 0,
      enableVib: false,
      wsUpdateObj: {type:"UPDATE"}
    };
  },
  methods: {
    changeSlider: function(val) {
      
       this.$socket.send( 'CHANGE_VIB_VAL '+this.sliderVal )
    },
    enableBluetooth : function(enable) {
       if(enable){
         this.$socket.send( 'VIB_CONNECT' )
       }else{
         this.$socket.send( 'VIB_DISCONNECT' )
       }
    },
    changeCheckbox: function(val) {
       if(this.enableVib){
         this.sliderVal = 0;
        this.$socket.send( 'VIB_STATE_ENABLE' )
       }else{
        this.$socket.send( 'VIB_STATE_DISABLE' )
       }
       
    },
  },
   created: function () {

     this.$options.sockets.onmessage = function(data) {
       let dat = JSON.parse( data.data )
       console.log(dat)
        
        if(dat.type === "SMARTPHONE"){
          console.log("xxx")
          this.smartphoneConneted = (dat.val === "true")?true:false;
          this.enableVib = false;
        }
        else if(dat.type === "BLUETOOTH"){
          this.bluetoothConneted = (dat.val === "true")?true:false;
          this.enableVib = false;
        }
        else if(dat.type === "CHANGE_VIB_STATE"){
          
          if(dat.val === "true"){
            this.enableVib = true;
            this.sliderVal = 0;
          }else{
            this.enableVib = false;
          }

         
        }
        else if(dat.type === "CHANGE_VIB_VAL"){
            this.sliderVal = parseInt(dat.val);
        }
        
     }

     this.$options.sockets.onopen = function(data) {
        this.$socket.send( 'UPDATE' )
        
     }


   }
  
}
</script>

<style>
#app {
  font-family: 'Avenir', Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  margin-top: 60px;
}
</style>
