<template>
  <div class="admin-reports">
    <h2>举报管理</h2>
    <el-table :data="reports" stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="targetType" label="类型" width="100" />
      <el-table-column prop="targetId" label="目标ID" width="80" />
      <el-table-column prop="reason" label="举报原因" />
      <el-table-column prop="reporterName" label="举报人" width="120" />
      <el-table-column label="状态" width="100"><template #default="{row}"><el-tag>{{ row.status }}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="200">
        <template #default>
          <el-button size="small" type="success">处理</el-button>
          <el-button size="small" type="info">忽略</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="reports.length === 0" description="暂无举报" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
const reports = ref([])
</script>
