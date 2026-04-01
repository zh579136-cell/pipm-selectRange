import axios from 'axios';
import { Message } from 'element-ui';
import state from '../store/appState';

const http = axios.create({
  baseURL: '/api'
});

http.interceptors.request.use(config => {
  const nextConfig = { ...config };
  nextConfig.headers = nextConfig.headers || {};
  nextConfig.headers['x-demo-user-id'] = state.currentUserId;
  return nextConfig;
});

http.interceptors.response.use(
  response => response,
  error => {
    const responseMessage = error && error.response && error.response.data && error.response.data.message;
    const message = responseMessage || error.message || '请求失败';
    Message.error(message);
    return Promise.reject(error);
  }
);

export default http;
