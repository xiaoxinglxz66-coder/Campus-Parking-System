// src/utils/roleHelper.js

/**
 * 获取当前用户的角色（永远从Token获取，最可靠）
 * @returns {string} 用户角色（ADMIN, STUDENT, TEACHER, STAFF, EXTERNAL_USER）
 */
export function getCurrentUserRole() {
  console.log('🔍 开始获取用户角色');

  // 第一步：尝试从Token获取（最权威）
  const token = localStorage.getItem('token');

  if (token) {
    try {
      // 解析Token的payload部分
      const payloadBase64 = token.split('.')[1];
      const payloadJson = atob(payloadBase64);
      const payload = JSON.parse(payloadJson);

      const tokenRole = payload.role;
      console.log('✅ 从Token获取角色:', tokenRole);

      if (tokenRole) {
        // 立即修复其他存储中的角色信息
        fixUserRoleInStorage(tokenRole);
        return tokenRole;
      }
    } catch (error) {
      console.error('❌ 从Token解析角色失败:', error);
    }
  }

  console.log('⚠️ Token中没有角色，尝试备用方案');

  // 第二步：备用方案，从其他存储位置获取
  return getRoleFromBackup();
}

/**
 * 修复存储中的角色信息，确保所有地方一致
 */
function fixUserRoleInStorage(correctRole) {
  console.log('🔧 开始修复存储中的角色信息...');

  try {
    // 1. 修复userInfo
    const userInfoStr = localStorage.getItem('userInfo');
    if (userInfoStr) {
      try {
        const userInfo = JSON.parse(userInfoStr);

        // 检查是否不一致
        if (userInfo.role !== correctRole) {
          console.log('⚠️ 发现不一致: userInfo.role =', userInfo.role, '正确角色 =', correctRole);

          // 修复它
          userInfo.role = correctRole;
          localStorage.setItem('userInfo', JSON.stringify(userInfo));
          console.log('✅ 已修复userInfo中的角色');
        }
      } catch (error) {
        console.error('解析userInfo失败，创建新的:', error);
        // 创建新的userInfo
        localStorage.setItem('userInfo', JSON.stringify({
          id: '',
          username: '',
          role: correctRole
        }));
      }
    }

    // 2. 修复单独的role字段
    localStorage.setItem('role', correctRole);

    // 3. 修复userRole字段
    localStorage.setItem('userRole', correctRole);

    console.log('🎉 角色信息修复完成');

  } catch (error) {
    console.error('❌ 修复角色信息失败:', error);
  }
}

/**
 * 从备用位置获取角色
 */
function getRoleFromBackup() {
  // 按优先级尝试不同的位置
  const backupRoles = [
    localStorage.getItem('role'),
    localStorage.getItem('userRole'),
    () => {
      try {
        const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
        return userInfo.role;
      } catch {
        return '';
      }
    }
  ];

  for (const backup of backupRoles) {
    const role = typeof backup === 'function' ? backup() : backup;

    if (role &&
      role !== 'undefined' &&
      role !== 'null' &&
      role.trim() !== '') {
      console.log('🔍 从备用位置获取角色:', role);
      return role;
    }
  }

  console.error('❌ 所有备用位置都没有找到角色');
  return '';
}
