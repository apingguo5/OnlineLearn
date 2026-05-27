<template>
  <div class="teacher-grading">
    <div class="page-header"><h2><i class="el-icon-document-checked"></i> 批改中心</h2></div>

    <el-tabs v-model="activeTab" type="border-card">
      <!-- ========== 试卷批改 ========== -->
      <el-tab-pane label="试卷批改" name="exam">
        <div class="filter-bar">
          <el-select v-model="filterPaperId" placeholder="筛选试卷" clearable size="small" style="width:200px">
            <el-option v-for="p in paperList" :key="p.id" :label="p.title" :value="p.id" />
          </el-select>
          <el-button type="primary" size="small" @click="loadExamPending">查询</el-button>
        </div>
        <el-table :data="examPendingList" v-loading="examLoading" border stripe style="width:100%;margin-top:10px">
          <el-table-column prop="studentName" label="学生" width="100" />
          <el-table-column prop="paperTitle" label="试卷" min-width="180" />
          <el-table-column prop="submitTime" label="提交时间" width="170" />
          <el-table-column label="操作" width="120" align="center" fixed="right">
            <template slot-scope="scope">
              <el-button type="primary" size="small" @click="startExamGrading(scope.row)">批改</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- ========== 作业批改 ========== -->
      <el-tab-pane label="作业批改" name="homework">
        <el-table :data="homeworkList" v-loading="hwLoading" border stripe style="width:100%">
          <el-table-column prop="title" label="作业标题" min-width="200" />
          <el-table-column label="操作" width="120" align="center">
            <template slot-scope="scope">
              <el-button type="primary" size="small" @click="selectHomework(scope.row)">查看提交</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div v-if="selectedHomeworkId" style="margin-top:16px">
          <h4 style="margin-bottom:10px">{{ selectedHomeworkTitle }} — 学生提交列表</h4>
          <el-table :data="submissionList" v-loading="subLoading" border stripe style="width:100%">
            <el-table-column prop="studentName" label="学生" width="100" />
            <el-table-column prop="submitTime" label="提交时间" width="170" />
            <el-table-column label="操作" width="180" align="center" fixed="right">
              <template slot-scope="scope">
                <el-button type="text" size="small" @click="viewSubmission(scope.row)">查看</el-button>
                <el-button type="primary" size="small" @click="openHomeworkGrading(scope.row)">批改</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- ========== 试卷逐题批改对话框 ========== -->
    <el-dialog :title="'批改试卷 — ' + examGradingStudentName" :visible.sync="examGradingVisible" width="800px" top="3vh" :append-to-body="false" @closed="resetExam">
      <div v-if="examQuestions.length" class="exam-grade-layout">
        <div class="eg-nav-bar">
          <div v-for="(q,i) in examQuestions" :key="i" class="eg-dot" :class="{'eg-dot-ok':q.manualScore!==undefined,'eg-dot-cur':egIdx===i}" @click="egIdx=i">{{ i+1 }}</div>
        </div>
        <div class="eg-main">
          <div class="eg-card" v-for="(q,idx) in examQuestions" :key="idx" v-show="egIdx===idx">
            <div class="eg-head">
              <el-tag :type="typeTag(q.questionType)" size="small">{{ typeLabel(q.questionType) }}</el-tag>
              <span class="eg-score">({{ q.score }}分)</span>
            </div>
            <div class="eg-stem"><b>{{ idx+1 }}.</b> {{ q.stem }}</div>

            <div v-if="q.questionType==='single'||q.questionType==='multiple'" class="eg-opts">
              <div v-for="(o,oi) in parsedOpts(q)" :key="oi" class="eg-opt-item">{{ opL(oi) }}. {{ o }}</div>
            </div>
            <div v-if="q.questionType==='judge'" class="eg-opts"><div class="eg-opt-item">正确 / 错误</div></div>

            <div class="eg-answer-row">
              <div class="eg-answer">
                <span class="eg-label">正确答案：</span><span class="eg-val">{{ formatAns(q) }}</span>
              </div>
              <div class="eg-answer">
                <span class="eg-label">学生答案：</span>
                <span class="eg-val" :class="{'eg-wrong':q.originalCorrect!==undefined&&!q.originalCorrect}">{{ formatStuAns(q) }}</span>
              </div>
            </div>

            <div class="eg-grade-row">
              <el-button-group size="small">
                <el-button :type="(q._correct===true)?'success':['default']" @click="markQ(q,true)">正确</el-button>
                <el-button :type="(q._correct===false)?'danger':['default']" @click="markQ(q,false)">错误</el-button>
                <el-button :type="(q._correct===undefined)?'info':['default']" @click="markQ(q,undefined)">待定</el-button>
              </el-button-group>
              <div class="eg-score-inp">
                <span>得分：</span>
                <el-input-number v-model="q.manualScore" :min="0" :max="q.score" size="small" :precision="1" />
                <span class="eg-score-max">/ {{ q.score }}分</span>
              </div>
              <div class="eg-remark-inp">
                <span>评语：</span>
                <el-input v-model="q.remark" placeholder="可选" size="small" style="width:200px" />
              </div>
            </div>
          </div>
          <div class="eg-nav-foot">
            <el-button size="small" @click="prevEg" :disabled="egIdx<=0">上一题</el-button>
            <span>{{ egIdx+1 }}/{{ examQuestions.length }}</span>
            <el-button size="small" @click="nextEg" :disabled="egIdx>=examQuestions.length-1">下一题</el-button>
          </div>
        </div>
      </div>
      <span slot="footer">
        <el-button @click="examGradingVisible=false">取消</el-button>
        <el-button type="primary" @click="submitExamGrade" :loading="examGradingLoading">提交批改</el-button>
      </span>
    </el-dialog>

    <!-- ========== 作业逐题批改对话框（试卷类） ========== -->
    <el-dialog :title="'批改作业 — ' + hwGradingStudentName" :visible.sync="hwPaperDialogVisible" width="800px" top="3vh" :append-to-body="false" @closed="resetHwPaper">
      <div v-if="hwPaperQuestions.length" class="exam-grade-layout">
        <div class="eg-nav-bar">
          <div v-for="(q,i) in hwPaperQuestions" :key="i" class="eg-dot" :class="{'eg-dot-ok':q.manualScore!==undefined,'eg-dot-cur':hwPaperIdx===i}" @click="hwPaperIdx=i">{{ i+1 }}</div>
        </div>
        <div class="eg-main">
          <div class="eg-card" v-for="(q,idx) in hwPaperQuestions" :key="idx" v-show="hwPaperIdx===idx">
            <div class="eg-head">
              <el-tag :type="typeTag(q.questionType)" size="small">{{ typeLabel(q.questionType) }}</el-tag>
              <span class="eg-score">({{ q.score }}分)</span>
            </div>
            <div class="eg-stem"><b>{{ idx+1 }}.</b> {{ q.stem }}</div>
            <div v-if="q.questionType==='single'||q.questionType==='multiple'" class="eg-opts">
              <div v-for="(o,oi) in parsedOpts(q)" :key="oi" class="eg-opt-item">{{ opL(oi) }}. {{ o }}</div>
            </div>
            <div v-if="q.questionType==='judge'" class="eg-opts"><div class="eg-opt-item">正确 / 错误</div></div>
            <div class="eg-answer-row">
              <div class="eg-answer"><span class="eg-label">正确答案：</span><span class="eg-val">{{ formatAns(q) }}</span></div>
              <div class="eg-answer"><span class="eg-label">学生答案：</span><span class="eg-val">{{ formatStuAns(q) }}</span></div>
            </div>
            <div class="eg-grade-row">
              <el-button-group size="small">
                <el-button :type="(q._correct===true)?'success':['default']" @click="markQ(q,true)">正确</el-button>
                <el-button :type="(q._correct===false)?'danger':['default']" @click="markQ(q,false)">错误</el-button>
                <el-button :type="(q._correct===undefined)?'info':['default']" @click="markQ(q,undefined)">待定</el-button>
              </el-button-group>
              <div class="eg-score-inp"><span>得分：</span><el-input-number v-model="q.manualScore" :min="0" :max="q.score" size="small" :precision="1" /><span class="eg-score-max">/ {{ q.score }}分</span></div>
              <div class="eg-remark-inp"><span>评语：</span><el-input v-model="q.remark" placeholder="可选" size="small" style="width:200px" /></div>
            </div>
          </div>
          <div class="eg-nav-foot">
            <el-button size="small" @click="hwPaperIdx>0&&hwPaperIdx--" :disabled="hwPaperIdx<=0">上一题</el-button>
            <span>{{ hwPaperIdx+1 }}/{{ hwPaperQuestions.length }}</span>
            <el-button size="small" @click="hwPaperIdx<hwPaperQuestions.length-1&&hwPaperIdx++" :disabled="hwPaperIdx>=hwPaperQuestions.length-1">下一题</el-button>
          </div>
        </div>
      </div>
      <span slot="footer">
        <el-button @click="hwPaperDialogVisible=false">取消</el-button>
        <el-button type="danger" @click="rejectHwPaper" :loading="gradingSubmitting">打回重做</el-button>
        <el-button type="success" @click="approveHwPaper" :loading="gradingSubmitting">通过批改</el-button>
      </span>
    </el-dialog>

    <!-- ========== 简单文本作业批改对话框 ========== -->
    <el-dialog :title="'批改作业 — ' + hwGradingStudentName" :visible.sync="hwSimpleDialogVisible" width="700px" top="5vh" :append-to-body="false" @closed="resetHwSimple">
      <div class="simple-grade">
        <div class="grade-info-line">作业：{{ hwGradingTitle }} | 提交时间：{{ hwGradingSubmitTime }}</div>
        <el-divider />
        <div v-if="hwGradingReference" class="ref-box"><h4>参考答案</h4><div class="txt-content">{{ hwGradingReference }}</div></div>
        <div class="stu-ans-box"><h4>学生作答内容</h4><div class="txt-content">{{ hwGradingReply || '（无作答内容）' }}</div></div>
        <el-divider />
        <el-form label-width="80px">
          <el-form-item label="得分"><el-input-number v-model="hwGradeScore" :min="0" :max="100" :precision="1" /></el-form-item>
          <el-form-item label="评语"><el-input type="textarea" v-model="hwGradeRemark" :rows="3" placeholder="可选" /></el-form-item>
        </el-form>
      </div>
      <span slot="footer">
        <el-button type="danger" icon="el-icon-refresh-left" @click="rejectSimpleHw" :loading="gradingSubmitting">打回重做</el-button>
        <el-button @click="hwSimpleDialogVisible=false">取消</el-button>
        <el-button type="success" icon="el-icon-check" @click="approveSimpleHw" :loading="gradingSubmitting">通过批改</el-button>
      </span>
    </el-dialog>

    <!-- 查看作答对话框 -->
    <el-dialog title="查看作业作答" :visible.sync="viewVisible" width="600px" top="5vh" :append-to-body="false">
      <div class="grade-info-line">学生：{{ viewStudentName }} | 作业：{{ viewHomeworkTitle }} | 提交时间：{{ viewSubmitTime }}</div>
      <el-divider />
      <div v-if="viewReference" class="ref-box"><h4>参考答案</h4><div class="txt-content">{{ viewReference }}</div></div>
      <el-divider />
      <div class="stu-ans-box"><h4>作答内容</h4><div class="txt-content">{{ viewReply || '（无作答内容）' }}</div></div>
    </el-dialog>
  </div>
</template>

<script>
import { batchReviewAnswers, getPaperList, getStudentAnswerDetail, getPaperDetail } from '@/api/teacher/examApi'
import { getPublishedTasks } from '@/api/teacher/teacherApi'

export default {
  name: 'TeacherGrading',
  data() {
    return {
      activeTab: 'exam',
      // 试卷
      examLoading: false, paperList: [], filterPaperId: '', examPendingList: [],
      examGradingVisible: false, examGradingLoading: false,
      examGradingStudentName: '', egIdx: 0, examQuestions: [], examRecordId: null,
      // 作业列表
      hwLoading: false, homeworkList: [],
      selectedHomeworkId: null, selectedHomeworkTitle: '',
      subLoading: false, submissionList: [],
      // 试卷类作业批改
      hwPaperDialogVisible: false, hwPaperIdx: 0, hwPaperQuestions: [],
      hwPaperRecordId: null, hwPaperStudentName: '', hwPaperHomeworkId: null,
      // 简单文本作业批改
      hwSimpleDialogVisible: false, gradingSubmitting: false,
      hwGradingRecordId: null, hwGradingStudentName: '', hwGradingTitle: '', hwGradingSubmitTime: '',
      hwGradingReply: '', hwGradingReference: '', hwGradeScore: 0, hwGradeRemark: '',
      // 查看
      viewVisible: false, viewStudentName: '', viewHomeworkTitle: '', viewSubmitTime: '', viewReply: '', viewReference: ''
    }
  },
  created() { this.loadPapers(); this.loadExamPending(); this.loadHomeworkList() },
  methods: {
    opL(i) { return String.fromCharCode(65+i) },
    typeLabel(t) { return {single:'单选',multiple:'多选',judge:'判断',fill:'填空',essay:'主观'}[t]||t },
    typeTag(t) { return {single:'primary',multiple:'success',judge:'warning',fill:'info',essay:''}[t]||'' },
    mapType(n) { return {1:'single',2:'multiple',3:'judge',4:'fill',5:'essay'}[n]||'essay' },
    parsedOpts(q) {
      if (!q||!q.options) return []
      let r=q.options
      if(typeof r==='string'){try{r=JSON.parse(r)}catch(e){r=r.split(',')}}
      if(!Array.isArray(r)) return []
      return r.map(o=>{if(o===null||o===undefined)return'';if(typeof o==='string')return o;if(typeof o==='object')return o.text||o.label||o.value||'';return String(o)})
    },
    formatAns(q) {
      const a=q.correctAnswer||''
      if(q.questionType==='judge') return a==='true'||a===true?'正确':(a==='false'||a===false?'错误':a)
      return a
    },
    formatStuAns(q) {
      const a=q.studentAnswer; if(!a&&a!==0) return '未作答'; return String(a)
    },
    markQ(q,val) { q._correct=val; if(val===true) q.manualScore=q.score; else if(val===false) q.manualScore=0; else q.manualScore=q.manualScore||0 },
    prevEg() { if(this.egIdx>0) this.egIdx-- },
    nextEg() { if(this.egIdx<this.examQuestions.length-1) this.egIdx++ },

    // ===== 试卷批改 =====
    async loadPapers() { try { const r=await getPaperList({page:1,limit:999}); const d=r.data&&r.data.resultData; this.paperList=(d&&d.data)?d.data:[] } catch(e){this.paperList=[]} },
    async loadExamPending() {
      this.examLoading=true
      try { const r=await this.$post('/study/exam/answer/paperStudentList',this.filterPaperId?{paperId:this.filterPaperId}:{}); const d=(r.data&&r.data.resultData)?r.data.resultData:[]; this.examPendingList=Array.isArray(d)?d:(d.data||[]) } catch(e){this.examPendingList=[]}
      this.examLoading=false
    },
    async startExamGrading(row) {
      this.examGradingStudentName=row.studentName; this.examRecordId=row.id; this.egIdx=0
      try {
        const r=await getStudentAnswerDetail({paperId:row.paperId,studentId:row.studentId})
        const list=(r.data&&r.data.resultData)?r.data.resultData:[]
        this.examQuestions=list.map(q=>{
          const qt=this.mapType(q.questionType)
          const correct=q.reviewStatus===1?true:(q.score!==null&&q.score!==undefined&&parseFloat(q.score)>0)
          return{
            recordId:q.id,questionType:qt,stem:q.stem||'',options:q.options||'',
            correctAnswer:q.correctAnswer||'',studentAnswer:q.answer||'',
            score:q.questionScore||0,manualScore:q.score||0,remark:q.remark||'',
            _correct:correct,originalCorrect:correct
          }
        })
        if(!this.examQuestions.length){this.$message.info('无答题详情');return}
        this.examGradingVisible=true
      } catch(e){this.$message.error('获取答题详情失败')}
    },
    async submitExamGrade() {
      this.examGradingLoading=true
      try{
        const list=this.examQuestions.map(q=>({recordId:q.recordId,score:q.manualScore,remark:q.remark||''}))
        if(!list.length){this.examGradingLoading=false;return}
        await batchReviewAnswers(list); this.$message.success('批改提交成功'); this.examGradingVisible=false; this.loadExamPending()
      } catch(e){this.$message.error('批改提交失败')}
      this.examGradingLoading=false
    },
    resetExam() { this.examQuestions=[]; this.examGradingStudentName=''; this.egIdx=0 },

    // ===== 作业批改 =====
    async loadHomeworkList() { try{this.hwLoading=true;const r=await getPublishedTasks({type:'homework'});const d=r.data&&r.data.resultData;this.homeworkList=Array.isArray(d)?d:(d&&d.data?d.data:[])}catch(e){this.homeworkList=[]}this.hwLoading=false },
    async selectHomework(row) {
      this.selectedHomeworkId=row.id; this.selectedHomeworkTitle=row.title; this.subLoading=true
      try{const r=await this.$post('/study/userdohomework/listSubmissions',{homeworkId:row.id});const d=r.data&&r.data.resultData;this.submissionList=Array.isArray(d)?d:(d&&d.data?d.data:[])}catch(e){this.submissionList=[]}
      this.subLoading=false
    },
    viewSubmission(row) {
      this.viewStudentName=row.studentName; this.viewHomeworkTitle=this.selectedHomeworkTitle||row.homeworkName
      this.viewSubmitTime=row.submitTime; this.viewReply=row.content||''; this.viewReference=row.reference||''
      this.viewVisible=true
    },
    async openHomeworkGrading(row) {
      const hw=this.homeworkList.find(h=>h.id===row.homeworkId)
      let paperId=null
      if(hw&&hw.content){try{const cd=JSON.parse(hw.content);if(cd&&cd.paperRef) paperId=cd.paperRef}catch(e){}}
      let answers=null
      try{answers=JSON.parse(row.content)}catch(e){}
      if(paperId && Array.isArray(answers)){
        // 试卷类作业，加载题目逐题批改
        try{
          const pr=await getPaperDetail(paperId)
          const paper=(pr.data&&pr.data.resultData)?pr.data.resultData:{}
          const amap={};(answers||[]).forEach(a=>{amap[a.questionId]=a.answer})
          this.hwPaperQuestions=(paper.questions||[]).map(q=>{
            const qt=this.mapType(q.questionType)
            const sa=amap[q.id]
            return{
              questionType:qt, stem:q.stem||'', options:q.options||'',
              correctAnswer:q.answer||'', studentAnswer:sa||'',
              score:q.score||5, manualScore:0, remark:'', _correct:undefined
            }
          })
          this.hwPaperRecordId=row.id; this.hwPaperStudentName=row.studentName; this.hwPaperHomeworkId=row.homeworkId; this.hwPaperIdx=0
          this.hwPaperDialogVisible=true
        } catch(e){this.$message.error('加载试卷题目失败');return}
      } else {
        // 简单文本作业
        this.hwGradingRecordId=row.id; this.hwGradingStudentName=row.studentName
        this.hwGradingTitle=row.homeworkName||this.selectedHomeworkTitle; this.hwGradingSubmitTime=row.submitTime
        this.hwGradingReply=row.content||''; this.hwGradingReference=row.reference||''
        this.hwGradeScore=0; this.hwGradeRemark=''; this.hwSimpleDialogVisible=true
      }
    },
    resetHwPaper() { this.hwPaperQuestions=[]; this.hwPaperIdx=0 },
    resetHwSimple() { this.hwGradingRecordId=null; this.hwGradingReply=''; this.hwGradingReference='' },
    async approveHwPaper() {
      this.gradingSubmitting=true
      const list=this.hwPaperQuestions.map(q=>({questionId:q.questionId,score:q.manualScore,remark:q.remark||''}))
      try{await this.$post('/study/userdohomework/approve',{recordId:this.hwPaperRecordId,homeworkId:this.hwPaperHomeworkId,scores:list,status:'approved'})
        this.$message.success('批改通过'); this.hwPaperDialogVisible=false; this.selectHomework({id:this.selectedHomeworkId,title:this.selectedHomeworkTitle})
      } catch(e){this.$message.error('操作失败')}
      this.gradingSubmitting=false
    },
    async rejectHwPaper() {
      this.gradingSubmitting=true
      try{await this.$post('/study/userdohomework/reject',{recordId:this.hwPaperRecordId,homeworkId:this.hwPaperHomeworkId})
        this.$message.success('已打回'); this.hwPaperDialogVisible=false; this.selectHomework({id:this.selectedHomeworkId,title:this.selectedHomeworkTitle})
      } catch(e){this.$message.error('操作失败')}
      this.gradingSubmitting=false
    },
    async approveSimpleHw() {
      this.gradingSubmitting=true
      try{await this.$post('/study/userdohomework/approve',{recordId:this.hwGradingRecordId,score:this.hwGradeScore,remark:this.hwGradeRemark,status:'approved'})
        this.$message.success('批改通过'); this.hwSimpleDialogVisible=false; this.selectHomework({id:this.selectedHomeworkId,title:this.selectedHomeworkTitle})
      } catch(e){this.$message.error('操作失败')}
      this.gradingSubmitting=false
    },
    async rejectSimpleHw() {
      this.gradingSubmitting=true
      try{await this.$post('/study/userdohomework/reject',{recordId:this.hwGradingRecordId})
        this.$message.success('已打回'); this.hwSimpleDialogVisible=false; this.selectHomework({id:this.selectedHomeworkId,title:this.selectedHomeworkTitle})
      } catch(e){this.$message.error('操作失败')}
      this.gradingSubmitting=false
    }
  }
}
</script>

<style scoped>
.teacher-grading { padding: 16px; background: #f5f7fa; min-height: 100%; }
.page-header { margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 20px; color: #303133; }

/* 试卷批改布局 */
.exam-grade-layout { display: flex; gap: 12px; }
.eg-nav-bar { width: 110px; flex-shrink: 0; background: #fafafa; border-radius: 8px; padding: 10px; display: flex; flex-wrap: wrap; gap: 6px; align-content: flex-start; max-height: 60vh; overflow-y: auto; }
.eg-dot { width: 30px; height: 30px; display: flex; align-items: center; justify-content: center; border-radius: 50%; font-size: 12px; font-weight: 700; cursor: pointer; background: #e4e7ed; color: #909399; transition: all .15s; }
.eg-dot:hover { background: #c6d9f0; }
.eg-dot.eg-dot-ok { background: #67C23A; color: #fff; }
.eg-dot.eg-dot-cur { border: 2px solid #303133; }
.eg-main { flex: 1; min-width: 0; }
.eg-card { background: #fff; border-radius: 8px; padding: 20px; border: 1px solid #ebeef5; min-height: 280px; }
.eg-head { margin-bottom: 10px; display: flex; align-items: center; gap: 8px; }
.eg-score { color: #909399; font-size: 13px; }
.eg-stem { font-size: 15px; line-height: 1.6; color: #303133; margin-bottom: 16px; }
.eg-opts { display: flex; flex-direction: column; gap: 6px; margin-bottom: 14px; }
.eg-opt-item { padding: 8px 12px; background: #f8f8f8; border-radius: 4px; font-size: 14px; }
.eg-answer-row { margin-bottom: 14px; }
.eg-answer { margin-bottom: 4px; }
.eg-label { color: #909399; font-size: 13px; }
.eg-val { color: #303133; font-weight: 600; }
.eg-val.eg-wrong { color: #F56C6C; }
.eg-grade-row { display: flex; align-items: center; flex-wrap: wrap; gap: 10px; }
.eg-score-inp { display: flex; align-items: center; gap: 4px; }
.eg-score-max { color: #909399; font-size: 12px; margin-left: 2px; }
.eg-remark-inp { display: flex; align-items: center; gap: 4px; }
.eg-nav-foot { display: flex; justify-content: center; align-items: center; gap: 14px; margin-top: 14px; padding-top: 10px; border-top: 1px solid #ebeef5; }

/* 简单批改 */
.simple-grade { }
.grade-info-line { color: #606266; margin-bottom: 8px; }
.ref-box, .stu-ans-box { margin-bottom: 10px; }
.ref-box h4, .stu-ans-box h4 { margin: 0 0 6px; font-size: 14px; }
.txt-content { font-size: 14px; line-height: 1.8; color: #606266; white-space: pre-wrap; }
</style>
