package fr.slixe.dero4j.util;

import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Map;

public class Helper
{

	public static JSONObject json(String method)
	{
		return json(method, null);
	}

	public static JSONObject json(String method, Map<String, Object> params)
	{
		JSONObject json = new JSONObject().put("jsonrpc", "2.0").put("id", "1").put("method", method);
		if (params != null) json.put("params", params);
		return json;
	}

	public static BigInteger asUint64(BigDecimal value, int scale)
	{
		return value.scaleByPowerOfTen(scale).toBigIntegerExact();
	}

	public static BigDecimal toBigDecimal(BigInteger value, int scale)
	{
		BigDecimal bigDecimal = new BigDecimal(Math.pow(10, scale));
		return new BigDecimal(value).divide(bigDecimal).setScale(scale, RoundingMode.HALF_DOWN);
	}
}
