<template>
    <div style="height: 80vh;overflow-y: auto;"> 
        

        <el-card v-for="item in points " class="box-card" :key="item.id"> 
            <div slot="header" class="clearfix">
                <span>{{ item.title }}</span>
            </div>
            <div class="text item">
                {{ item.content }} 
            </div>
        </el-card>
        
    </div>
</template>
 
<script>
import {stuPointList} from '@/api/admin/knowpoint'
import Cookies from "js-cookie";
export default {
    name: "point",
    components: {},
    data() {
        return {
           points:[]
        }
    },
    methods: {},
    created() {
        let classId=Cookies.get("classId")
        stuPointList({classId:classId}).then(res=>{
            this.points=res.data.resultData
            console.log(this.points);    
        })

     }
}
</script>
<style scoped>
.el-card{
    margin-top: 20px;  
}
</style>
