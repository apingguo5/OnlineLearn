import { post } from "../api/request";
import Cookies from "js-cookie";

export class WatchTimeService {
  constructor(options = {}) {
    this.userId = options.userId || Cookies.get('userId');
    this.roleId = options.roleId || Cookies.get('roleId');
    this.currentVideoId = null;
    this.currentVideoTotalId = null;
    this.watchStartTime = null;
    this.pauseStartTime = null;
    this.realTimeWatchTime = 0;
    this.accumulatedWatchTime = 0;
    this.realTimeTimer = null;
    this.saveDebounceTimer = null;
    this.autoSaveTimer = null;
    this.saveInterval = 60000; // 60秒自动保存一次
    this.minWatchTime = 5; // 最小保存时长（秒）
    this.isPaused = false;
    
    this.onWatchTimeUpdate = options.onWatchTimeUpdate || (() => {});
    
    // 初始化时尝试从本地存储恢复状态
    this._loadFromLocalStorage();
  }

  startWatch(videoId, videoTotalId) {
    if (this.roleId != 3) return;
    
    this.currentVideoId = videoId;
    this.currentVideoTotalId = videoTotalId;
    this.watchStartTime = new Date().getTime();
    this.isPaused = false;
    
    this._getAccumulatedWatchTime(videoId);
    this._startRealTimeTimer();
    this._startAutoSaveTimer();
  }

  // 暂停观看
  pauseWatch() {
    if (this.watchStartTime && !this.isPaused) {
      this.pauseStartTime = new Date().getTime();
      this.isPaused = true;
      this._stopRealTimeTimer();
    }
  }

  // 恢复观看
  resumeWatch() {
    if (this.watchStartTime && this.isPaused && this.pauseStartTime) {
      const pauseDuration = Math.floor((new Date().getTime() - this.pauseStartTime) / 1000);
      // 调整开始时间，减去暂停的时长
      this.watchStartTime += pauseDuration * 1000;
      this.isPaused = false;
      this.pauseStartTime = null;
      this._startRealTimeTimer();
      this._startAutoSaveTimer();
    }
  }

  stopWatch() {
    if (this.watchStartTime && this.currentVideoId) {
      let endTime = new Date().getTime();
      
      // 如果视频处于暂停状态，使用暂停时的时间
      if (this.isPaused && this.pauseStartTime) {
        endTime = this.pauseStartTime;
      }
      
      const sessionWatchTime = Math.floor((endTime - this.watchStartTime) / 1000);
      
      if (sessionWatchTime >= this.minWatchTime) {
        this._saveWatchTime(sessionWatchTime);
        this.accumulatedWatchTime += sessionWatchTime;
      }
      
      this._resetState();
      this._saveToLocalStorage();
    }
  }

  switchVideo(videoId, videoTotalId) {
    this.stopWatch();
    this.startWatch(videoId, videoTotalId);
  }

  getWatchTime() {
    return this.accumulatedWatchTime + this.realTimeWatchTime;
  }

  destroy() {
    this.stopWatch();
  }

  _getAccumulatedWatchTime(videoId) {
    if (!this.userId) return;
    
    // 先尝试从本地存储获取
    const localData = this._getLocalWatchTime(videoId);
    if (localData) {
      this.accumulatedWatchTime = localData;
      this._notifyUpdate();
    }
    
    // 再从服务器获取最新数据
    post('/study/videoWatchRecord/queryByUserId', { userId: this.userId })
      .then(resp => {
        if (resp.data.code == 200) {
          const records = resp.data.resultData;
          const record = records.find(item => item.videoId == videoId);
          this.accumulatedWatchTime = record ? (record.watchTime || 0) : 0;
          this._saveLocalWatchTime(videoId, this.accumulatedWatchTime);
          this._notifyUpdate();
        }
      })
      .catch(error => {
        console.error('获取累计观看时长失败:', error);
        // 失败时使用本地存储的数据
      });
  }

  _saveWatchTime(watchTime) {
    if (this.roleId != 3 || !this.userId || watchTime < this.minWatchTime) return;
    
    const params = {
      userId: this.userId,
      videoId: this.currentVideoId,
      videoTotalId: this.currentVideoTotalId,
      watchTime: watchTime
    };
    
    // 计算新的累计时长
    const newAccumulatedWatchTime = this.accumulatedWatchTime + watchTime;
    
    // 防抖处理，避免频繁保存
    clearTimeout(this.saveDebounceTimer);
    this.saveDebounceTimer = setTimeout(() => {
      post('/study/videoWatchRecord/saveWatchTime', params)
        .then(resp => {
          if (resp.data.code == 200) {
            console.log('观看时长保存成功:', watchTime, '秒');
            // 更新本地存储
            this._saveLocalWatchTime(this.currentVideoId, newAccumulatedWatchTime);
          } else {
            console.error('观看时长保存失败');
          }
        })
        .catch(error => {
          console.error('保存观看时长时发生错误:', error);
          // 保存失败时，将数据存储到本地，待下次重试
          this._saveLocalWatchTime(this.currentVideoId, newAccumulatedWatchTime);
        });
    }, 1000);
  }

  _startRealTimeTimer() {
    this._stopRealTimeTimer();
    this.realTimeWatchTime = 0;
    this.realTimeTimer = setInterval(() => {
      // 只有在视频未暂停时才更新实时观看时长
      if (!this.isPaused) {
        this.realTimeWatchTime++;
        this._notifyUpdate();
      }
    }, 1000);
  }

  _startAutoSaveTimer() {
    // 每60秒自动保存一次
    this.autoSaveTimer = setInterval(() => {
      if (this.watchStartTime && this.currentVideoId && !this.isPaused) {
        const currentTime = new Date().getTime();
        const sessionWatchTime = Math.floor((currentTime - this.watchStartTime) / 1000);
        
        if (sessionWatchTime >= this.minWatchTime) {
          this._saveWatchTime(sessionWatchTime);
          // 重置开始时间，避免重复保存
          this.watchStartTime = currentTime;
          this.accumulatedWatchTime += sessionWatchTime;
          this.realTimeWatchTime = 0;
        }
      }
    }, this.saveInterval);
  }

  _stopRealTimeTimer() {
    if (this.realTimeTimer) {
      clearInterval(this.realTimeTimer);
      this.realTimeTimer = null;
    }
    if (this.autoSaveTimer) {
      clearInterval(this.autoSaveTimer);
      this.autoSaveTimer = null;
    }
  }

  _resetState() {
    this.watchStartTime = null;
    this.pauseStartTime = null;
    this.currentVideoId = null;
    this.currentVideoTotalId = null;
    this.realTimeWatchTime = 0;
    this.isPaused = false;
    this._stopRealTimeTimer();
  }

  _notifyUpdate() {
    if (typeof this.onWatchTimeUpdate === 'function') {
      this.onWatchTimeUpdate(this.getWatchTime());
    }
  }

  // 本地存储相关方法
  _saveToLocalStorage() {
    if (!this.userId) return;
    
    try {
      const state = {
        accumulatedWatchTime: this.accumulatedWatchTime,
        currentVideoId: this.currentVideoId
      };
      localStorage.setItem(`watchTime_${this.userId}`, JSON.stringify(state));
    } catch (error) {
      console.error('保存到本地存储失败:', error);
    }
  }

  _loadFromLocalStorage() {
    if (!this.userId) return;
    
    try {
      const state = localStorage.getItem(`watchTime_${this.userId}`);
      if (state) {
        const parsedState = JSON.parse(state);
        this.accumulatedWatchTime = parsedState.accumulatedWatchTime || 0;
        this.currentVideoId = parsedState.currentVideoId;
      }
    } catch (error) {
      console.error('从本地存储加载失败:', error);
    }
  }

  _saveLocalWatchTime(videoId, watchTime) {
    if (!this.userId) return;
    
    try {
      const key = `watchTime_${this.userId}_${videoId}`;
      localStorage.setItem(key, watchTime.toString());
    } catch (error) {
      console.error('保存视频观看时长到本地存储失败:', error);
    }
  }

  _getLocalWatchTime(videoId) {
    if (!this.userId) return null;
    
    try {
      const key = `watchTime_${this.userId}_${videoId}`;
      const watchTime = localStorage.getItem(key);
      return watchTime ? parseInt(watchTime, 10) : null;
    } catch (error) {
      console.error('从本地存储获取视频观看时长失败:', error);
      return null;
    }
  }
}
