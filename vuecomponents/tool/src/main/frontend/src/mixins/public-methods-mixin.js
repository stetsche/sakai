export default {
  mounted: async function() {
    const $vm = this;
    
    setTimeout(() => {
      const host = $vm.$refs.component.getRootNode().host//.host;
      console.log("host", host);
    }, 500)
    // host.test = function() {
    //   console.log("TEST", $vm.$data);
    // }
  }
};
