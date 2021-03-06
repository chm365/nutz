package org.nutz.resource.impl;

import java.io.File;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.nutz.lang.Encoding;
import org.nutz.lang.Files;
import org.nutz.lang.util.Disks;
import org.nutz.resource.JarEntryInfo;
import org.nutz.resource.NutResource;

/**
 * 针对本地文件系统，递归扫描一组特定的资源（不包括目录）
 * <ul>
 * <li>参数 <b>src</b> : 表示特定资源参考路径，可以是一个文件或者目录或者 jar 中的实体
 * <li>参数 <b>filter</b> : 将被作为一个正则表达式，来匹配资源名（注，不是全路径名，仅仅是名称）
 * </ul>
 * <p>
 * 特别需要说明的是
 * <ul>
 * <li>如果你要寻找的资源在一个 jar 里面，你的参考路径必须为一个具体的实体。
 * <li>默认的，所有的隐藏文件，将被忽略，如果想修改这个设置，请调用 setIgnoreHidden(false)
 * </ul>
 * 
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class LocalResourceScan extends AbstractResourceScan {

	private boolean ignoreHidden;

	/**
	 * 是否忽略隐藏文件
	 * 
	 * @param ignoreHidden
	 *            ： true 忽略， false 不忽略
	 * @return 自身
	 */
	public LocalResourceScan setIgnoreHidden(boolean ignoreHidden) {
		this.ignoreHidden = ignoreHidden;
		return this;
	}

	public LocalResourceScan() {
		this(true);
	}

	public LocalResourceScan(boolean ignoreHidden) {
		this.ignoreHidden = ignoreHidden;
	}

	public List<NutResource> list(String src, String filter) {
		final List<NutResource> list = new LinkedList<NutResource>();
		final Pattern regex = null == filter ? null : Pattern.compile(filter);
		// 查看资源是否存在在磁盘系统中
		File f = Files.findFile(src);

		// 如果存在，递归这个目录
		if (f != null && f.exists()) {
			if (f.isFile())
				list.addAll(scanInDir(regex, f.getParentFile(), ignoreHidden));
			else
				list.addAll(scanInDir(regex, f, ignoreHidden));
		}
		// 查看资源是否存在在 CLASSPATH 中
		else {
			// 如果在其中，那么是在一个 JAR 中还是在一个本地目录里
			String path = Disks.absolute(	src,
											getClass().getClassLoader(),
											Encoding.defaultEncoding());
			if (null != path) {
				f = new File(path);
				// 如果是本地目录，递归这个目录
				if (!path.contains(".jar!")) {
					// 首先查找以下， CLASSPATH 从哪里开始
					int pos = path.lastIndexOf(src);
					if (pos > 0)
						list.addAll(scanInDir(regex, f, ignoreHidden));
				}
				// 如果在 jar 中，则循环查找这个 jar 的每一个实体
				else {
					JarEntryInfo jeInfo = new JarEntryInfo(path);
					list.addAll(scanInJar(	checkSrc(jeInfo.getEntryName()),
											regex,
											jeInfo.getJarPath()));
				}
			} else {
				try {
					Enumeration<URL> en = getClass().getClassLoader().getResources(src);
					if (en != null) {
						while (en.hasMoreElements()) {
							JarEntryInfo jeInfo = new JarEntryInfo(en.nextElement().getPath());
							list.addAll(scanInJar(	checkSrc(jeInfo.getEntryName()),
													regex,
													jeInfo.getJarPath()));
						}
					}
				}
				catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}
		if (list.isEmpty())
			scanClasspath(src, regex, list);
		// 返回资源列表
		return list;
	}

	/**
	 * 用于Scans.java中,在jar包里面搜索对应的模块
	 * 
	 * @param src
	 * @param regex
	 * @param jarPath
	 * @return
	 * @see org.nutz.resource.impl.AbstractResourceScan#scanInJar(java.lang.String,
	 *      java.util.regex.Pattern, java.lang.String)
	 */
	public List<NutResource> scanInJar(String src, Pattern regex, String jarPath) {
		return super.scanInJar(src, regex, jarPath);
	}

	/**
	 * @param regex
	 * @param f
	 * @return
	 * @author replaceToYouName at 2012-1-12 上午9:15:02
	 */
	public List<NutResource> scanInDir(final Pattern regex, File f) {
		final boolean ignoreHidden = true;
		return super.scanInDir(regex, f, ignoreHidden);

	}
}
