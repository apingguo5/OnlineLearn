import Vue from 'vue'
import VueRouter from 'vue-router'
import Login from '../views/login/Login.vue'
import TeacherContainer from '../views/container/TeacherContainer.vue'
import StudentInfo from '../views/studentInfo/StudentInfo.vue'
import AdminHome from '../views/admin/AdminHome.vue'
import TeacherHome from '../views/container/TeacherHome.vue'
import ScoreManagment from '../views/scoremanagement/ScoreManagment.vue'
import ClassManagement from '../views/classmanagement/ClassManagement.vue'
import SubjectManagement from '../views/subjectmanagement/SubjectManagement.vue'
import CourseManagement from '../views/coursemanagement/CourseManagement.vue'
import PersonalInfo from '../views/personalInfo/PersonalInfo.vue'
import NoticeManagement from '../views/noticemanagement/NoticeManagement.vue'
import StudentContainWeb from '../views/studentweb/contain/StudentContainWeb.vue'
import AskAndAnswer from '../views/studentweb/askandanswer/AskAndAnswer.vue'
import StudentPractice from '../views/studentweb/practice/StudentPractice.vue'
import EssentiaInfo from '../views/studentweb/essentialinformation/EssentiaInfo.vue'
import AdminManagement from '../views/admin/AdminManagement.vue'
import MarkDown from '../views/studentweb/markdown/MarkDown.vue'
import DetaliCourse from '../views/studentweb/askandanswer/DetaliCourse.vue'
import home from '../views/studentweb/contain/home.vue'
import StudentCourses from '../views/studentweb/courses/StudentCourses.vue'
import StudentNotes from '../views/studentweb/notes/StudentNotes.vue'
import StudentInbox from '../views/studentweb/inbox/StudentInbox.vue'
import AdminStudentManagement from '../views/admin/studentmanagement/StudentManagement.vue'
import TeacherManagement from '../views/admin/teacherm/TeacherManagement.vue'
import AdminHomeWork from '../views/admin/adminhomework/AdminHomeWork.vue'
import TestManagement from '../views/admin/testmanagement/TestManagement.vue'
import KnowledgePoints from '../views/admin/knowledgepoints/KnowledgePoints.vue'
import AdminPersonalInfo from '../views/admin/personalInfo/PersonalInfo.vue'
import TeacherPersonalInfo from '../views/admin/personalInfo/PersonalInfo.vue'
import TeacherApplicant from '../views/teacherapplicant/TeacherApplicant.vue'
import DetailOnlineWeb from '../views/studentweb/onlinelearn/DetailOnlineWeb.vue'
import teacherVideo from '../views/studentweb/onlinelearn/DetailOnlineWeb.vue'
import PracticeDetail from '../views/studentweb/practice/practiceDetail/PracticeDetail.vue'
import HomeworkDetail from '../views/admin/adminhomework/HomeworkDetail.vue'
import TestHomeWork from '../views/admin/testmanagement/TestHomeWork.vue'
import KnowDetail from '../views/admin/knowledgepoints/KnowDetail.vue'
import Detail from '../views/scoremanagement/Detail.vue'
import StudentDetail from '../views/scoremanagement/Detail.vue'
import DetailMark from '../views/scoremanagement/DetailMark.vue'
import NotHomework from '../views/scoremanagement/NotHomework.vue'
import PDetail from '../views/personalInfo/PDetail.vue'
import Video from '../views/video/Video.vue'
import DoHomeWork from '../views/classmanagement/DoHomeWork.vue'
import NotHomeWork from '../views/classmanagement/NotHomeWork.vue'
import pDetail from '../views/studentweb/practice/practiceDetail/pDetail.vue'
import ClassManagementAdmin from '../views/admin/classmanagement/ClassManagement.vue'
import Subject from '../views/admin/subject/Subject.vue'
import WatchTime from '../views/studentweb/watchtime/WatchTime.vue'
import TeacherAskAndAnswer from '../views/container/askandanswer/TeacherAskAndAnswer.vue'
import TeacherDashboard from '../views/teacher/TeacherDashboard.vue'
import TeacherCourseManagement from '../views/teacher/TeacherCourseManagement.vue'
import TeacherCourseList from '../views/teacher/TeacherCourseList.vue'
import TeacherClassManagement from '../views/teacher/TeacherClassManagement.vue'
import TeacherAssessment from '../views/teacher/TeacherAssessment.vue'
import TeacherGradebook from '../views/teacher/TeacherGradebook.vue'
import TeacherCommunication from '../views/teacher/TeacherCommunication.vue'
import TestComponent from '../views/test/TestComponent.vue'
import SimpleTest from '../views/test/SimpleTest.vue'
Vue.use(VueRouter)

const routes = [
    {
        path: '/',
        redirect: '/login'
    },
    {
        path: '/test',
        name: 'TestComponent',
        component: TestComponent
    },
    {
        path: '/simple',
        name: 'SimpleTest',
        component: SimpleTest
    },
    {
        path: '/login',
        name: 'Login',
        component: Login
    }, {
        path: '/studentdetail',
        name: 'StudentDetail',
        component: StudentDetail,
        meta: {
            requireAuth: true,
        },
    }, {
        path: '/pDetail',
        name: 'pDetail',
        component: pDetail,
        meta: {
            requireAuth: true,
        },
    },
    {
        path: '/teachermanagement',
        name: 'teacherManagement',
        component: TeacherManagement,
        meta: {
            requireAuth: true,
        },
    },
    {
        path: '/detail',
        name: 'Detail',
        component: Detail,
        meta: {
            requireAuth: true,
        },
    }, {
        path: '/detailmark',
        name: 'DetailMark',
        component: DetailMark,
        meta: {
            requireAuth: true,
        },
    },
    {
        path: '/detailonlineweb',
        name: 'DetailOnlineWeb',
        component: DetailOnlineWeb,
        meta: {
            requireAuth: true,
        },
    }, {
        path: '/TestHomeWork',
        name: 'TestHomeWork',
        component: TestHomeWork,
        meta: {
            requireAuth: true,
        },
    }, {
        path: '/KnowDetail',
        name: 'KnowDetail',
        component: KnowDetail,
        meta: {
            requireAuth: true,
        },
    },
    {
        path: '/markdown',
        name: 'MarkDown',
        component: MarkDown,
        meta: {
            requireAuth: true,
        },
    },
    {
        path: '/homeworkdetail',
        name: 'HomeworkDetail',
        component: HomeworkDetail,
        meta: {
            requireAuth: true,
        },
    },
    {
        path: '/detalicourse',
        name: 'DetaliCourse',
        component: DetaliCourse,
        meta: {
            requireAuth: true,
        },
    },
    {
        path: '/adminmanagement',
        component: AdminManagement,
        meta: {
            requireAuth: true,
        },
        children: [
            {
                path: '/adminHome',
                name: 'AdminHome',
                component: AdminHome,
                meta: {
                    requireAuth: true,
                },
            },
            {
                path: '/adminstudentmanagement',
                name: 'AdminStudentManagement',
                component: AdminStudentManagement,
                meta: {
                    requireAuth: true,
                },
            },
            {
                path: '/subject',
                name: 'Subject',
                component: Subject,
                meta: {
                    requireAuth: true,
                },
            },
            {
                path: '/classmanagementadmin',
                name: 'ClassManagementAdmin',
                component: ClassManagementAdmin,
                meta: {
                    requireAuth: true,
                },
            },
            {
                path: '/teachermanagement',
                name: 'TeacherManagement',
                component: TeacherManagement,
                meta: {
                    requireAuth: true,
                },
            },
            {
                path: '/adminhomework',
                name: 'AdminHomeWork',
                component: AdminHomeWork,
                meta: {
                    requireAuth: true,
                },
            },
            {
                path: '/testmanagement',
                name: 'TestManagement',
                component: TestManagement,
                meta: {
                    requireAuth: true,
                },
            },
            {
                path: '/knowledgepoints',
                name: 'KnowledgePoints',
                component: KnowledgePoints,
                meta: {
                    requireAuth: true,
                },
            },
            {
                path: '/adminpersonalinfo',
                name: 'AdminPersonalInfo',
                component: AdminPersonalInfo,
                meta: {
                    requireAuth: true,
                },
            },
        ]
    },
    {
        path: '/studentweb',
        name: 'StudentContainWeb',
        component: StudentContainWeb,
        meta: {
            requireAuth: true,
        },
        redirect: { name: "studenthome" },
        children: [
            {
                path: '/studenthome',
                name: 'studenthome',
                component: home,
                meta: {
                    requireAuth: true,
                },
            },
            {
                path: '/studentcourses',
                name: 'StudentCourses',
                component: StudentCourses,
                meta: {
                    requireAuth: true,
                },
            },
            {
                path: '/studentnotes',
                name: 'StudentNotes',
                component: StudentNotes,
                meta: {
                    requireAuth: true,
                },
            },
            {
                path: '/studentinbox',
                name: 'StudentInbox',
                component: StudentInbox,
                meta: {
                    requireAuth: true,
                },
            },
            {
                path: '/home',
                name: 'home',
                component: home,
                meta: {
                    requireAuth: true,
                },
            },
            {
                path: '/practicedetail',
                name: 'PracticeDetail',
                component: PracticeDetail,
                meta: {
                    requireAuth: true,
                },
            },
            {
                path: '/askandanswer',
                name: 'AskAndAnswer',
                component: AskAndAnswer,
                meta: {
                    requireAuth: true,
                },
            },
            {
                path: '/studentpractice',
                name: 'StudentPractice',
                component: StudentPractice,
                meta: {
                    requireAuth: true,
                },
            },
            {
                path: '/essentiainfo',
                name: 'EssentiaInfo',
                component: EssentiaInfo,
                meta: {
                    requireAuth: true,
                },
            },
            {
                path: '/watchtime',
                name: 'WatchTime',
                component: WatchTime,
                meta: {
                    requireAuth: true,
                },
            },
        ]
    },
    {
        path: '/hometeacher',
        name: 'TeacherContainer',
        component: TeacherContainer,
        meta: {
            requireAuth: true,
        },
        children: [
            {
                path: '/teacherHome',
                name: 'TeacherHome',
                component: TeacherHome,
                meta: {
                    requireAuth: true,
                },
            },
            {
                path: '/studentinfo',
                name: 'StudentInfo',
                component: StudentInfo,
                meta: {
                    requireAuth: true,
                },
            },
            {
                path: '/dohomework',
                name: 'DoHomeWork',
                component: DoHomeWork,
                meta: {
                    requireAuth: true,
                },
            },
            {
                path: '/nothomework',
                name: 'NotHomeWork',
                component: NotHomeWork,
                meta: {
                    requireAuth: true,
                },
            },
            {
                path: '/teacherpersonalinfo',
                name: 'TeacherPersonalInfo',
                component: TeacherPersonalInfo,
                meta: {
                    requireAuth: true,
                },
            },
            {
                path: '/video',
                name: 'Video',
                component: Video,
                meta: {
                    requireAuth: true,
                },
            },
            {
                path: '/teachervideo',
                name: 'teacherVideo',
                component: teacherVideo,
                meta: {
                    requireAuth: true,
                },
            },
            {
                path: '/scoremanagment',
                name: 'ScoreManagment',
                component: ScoreManagment,
                meta: {
                    requireAuth: true,
                },
            },
            {
                path: '/classmanagement',
                name: 'ClassManagement',
                component: ClassManagement,
                meta: {
                    requireAuth: true,
                },
            },
            {
                path: '/subjectmanagement',
                name: 'SubjectManagement',
                component: SubjectManagement,
                meta: {
                    requireAuth: true,
                },
            },
            {
                path: '/coursemanagement',
                name: 'CourseManagement',
                component: CourseManagement,
                meta: {
                    requireAuth: true,
                },
            },
            {
                path: '/noticemanagement',
                name: 'NoticeManagement',
                component: NoticeManagement,
                meta: {
                    requireAuth: true,
                },
            },
            {
                path: '/personalinfo',
                name: 'PersonalInfo',
                component: PersonalInfo,
                meta: {
                    requireAuth: true,
                },
            },
            {
                path: '/teacherapplicant',
                name: 'TeacherApplicant',
                component: TeacherApplicant,
                meta: {
                    requireAuth: true,
                },
            },
            {
                path: '/teacheraskandanswer',
                name: 'TeacherAskAndAnswer',
                component: TeacherAskAndAnswer,
                meta: {
                    requireAuth: true,
                },
            },
            {
                path: '/teacherdashboard',
                name: 'TeacherDashboard',
                component: TeacherDashboard,
                meta: {
                    requireAuth: true,
                },
            },
            {
                path: '/teachercourselist',
                name: 'TeacherCourseList',
                component: TeacherCourseList,
                meta: {
                    requireAuth: true,
                },
            },
            {
                path: '/teachercoursemanagement/:courseId?',
                name: 'TeacherCourseManagement',
                component: TeacherCourseManagement,
                meta: {
                    requireAuth: true,
                },
            },
            {
                path: '/teacherclassmanagement',
                name: 'TeacherClassManagement',
                component: TeacherClassManagement,
                meta: {
                    requireAuth: true,
                },
            },
            {
                path: '/teacherassessment',
                name: 'TeacherAssessment',
                component: TeacherAssessment,
                meta: {
                    requireAuth: true,
                },
            },
            {
                path: '/teachergradebook',
                name: 'TeacherGradebook',
                component: TeacherGradebook,
                meta: {
                    requireAuth: true,
                },
            },
            {
                path: '/teachercommunication',
                name: 'TeacherCommunication',
                component: TeacherCommunication,
                meta: {
                    requireAuth: true,
                },
            },
        ]
    },
]

import Cookies from "js-cookie";

const router = new VueRouter({
    routes
})

router.beforeEach((to, from, next) => {
    if (to.meta.requireAuth) {
        if (Cookies.get("userId")) {
            next();
        }
        else {
            next({
                path: '/login',
                query: { redirect: to.fullPath }
            })
        }
    }
    else {
        next();
    }
})
export default router