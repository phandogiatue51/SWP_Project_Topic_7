import React, { useState, useEffect, useMemo } from "react";
import {
  Card,
  Row,
  Col,
  Typography,
  Spin,
  Divider,
  DatePicker,
  Space,
  Button,
} from "antd";
import {
  ComposedChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from "recharts";
import dayjs from "dayjs";
import { useGetOrderStatusChart } from "../../hooks/order/useGetOrderStatusChart";

const { Title } = Typography;
const { RangePicker } = DatePicker;

const OrderChart = ({onTotalOrdersChange}) => {
  const defaultStartDate = useMemo(() => dayjs("2024-09-01"), []);
  const [dateRange, setDateRange] = useState([defaultStartDate, dayjs()]);
  const [chartData, setChartData] = useState([]);

  const {
    data: orderStatusData,
    isLoading,
    isError,
    refetch,
  } = useGetOrderStatusChart(
    dateRange[0].format("YYYY/MM/DD"),
    dateRange[1].format("YYYY/MM/DD")
  );

  useEffect(() => {
    if (orderStatusData) {
      const formattedData = orderStatusData.data.reduce((acc, item) => {
        const date = dayjs(item.date).format("YYYY-MM-DD");
        if (!acc[date]) {
          acc[date] = { date, CANCELLED: 0, COMPLETED: 0 };
        }
        if (item.label === "CANCELLED") {
          acc[date].CANCELLED = item.count;
        } else if (item.label === "COMPLETED") {
          acc[date].COMPLETED = item.count;
        }
        return acc;
      }, {});

      const sortedData = Object.values(formattedData).sort((a, b) =>
        dayjs(a.date).diff(dayjs(b.date))
      );

      setChartData(sortedData);

      onTotalOrdersChange(totalOrders);
    }
  }, [orderStatusData, onTotalOrdersChange]);

  const totalOrders = useMemo(() => {
    return chartData.reduce((sum, item) => sum + item.CANCELLED + item.COMPLETED, 0);
  }, [chartData]);

  const handleDateRangeChange = (dates) => {
    if (dates && dates.length === 2) {
      setDateRange(dates);
    }
  };

  const formatXAxis = (tickItem) => {
    return dayjs(tickItem).format("MMM DD");
  };

  const disabledDate = (current) => {
    return current && current > dayjs().endOf("day");
  };

  if (isLoading) return <Spin size="large" />;
  if (isError) return <div>Error loading data. Please try again later.</div>;



  const cancelledOrders = chartData.reduce(
    (sum, item) => sum + (item.CANCELLED || 0),
    0
  );
  const completedOrders = chartData.reduce(
    (sum, item) => sum + (item.COMPLETED || 0),
    0
  );

  const rangePresets = [
    { label: "Last 7 Days", value: [dayjs().subtract(6, "day"), dayjs()] },
    { label: "Last 30 Days", value: [dayjs().subtract(29, "day"), dayjs()] },
    {
      label: "This Month",
      value: [dayjs().startOf("month"), dayjs().endOf("month")],
    },
    {
      label: "Last Month",
      value: [
        dayjs().subtract(1, "month").startOf("month"),
        dayjs().subtract(1, "month").endOf("month"),
      ],
    },
    { label: "Last 3 Months", value: [dayjs().subtract(3, "month"), dayjs()] },
    { label: "Last 6 Months", value: [dayjs().subtract(6, "month"), dayjs()] },
    {
      label: "Last 12 Months",
      value: [dayjs().subtract(12, "month"), dayjs()],
    },
    { label: "All Time", value: [defaultStartDate, dayjs()] },
  ];

  return (
    <div>
      <Title level={2} style={{ marginBottom: "24px", color: "#1890ff" }}>
        Order Statistics
      </Title>
      <Row justify="center" style={{ marginBottom: "20px" }}>
        <Col xs={24} sm={24} md={20} lg={16} xl={12}>
          <Space size={12} style={{ width: "100%", justifyContent: "center" }}>
            <RangePicker
              value={dateRange}
              onChange={handleDateRangeChange}
              format="YYYY/MM/DD"
              disabledDate={disabledDate}
              presets={rangePresets}
            />
            <Button
              type="primary"
              onClick={refetch}
              style={{ width: "calc(100% - 6px)" }}
            >
              Fetch Data
            </Button>
          </Space>
        </Col>
      </Row>
      <Divider style={{ marginBottom: "24px" }} />
      <Row gutter={[16, 16]}>
        <Col xs={24} md={8}>
          <Card title="Total Orders" className="stat-card">
            <Title level={4}>{totalOrders}</Title>
          </Card>
        </Col>
        <Col xs={24} md={8}>
          <Card title="Cancelled Orders" className="stat-card">
            <Title level={4}>{cancelledOrders}</Title>
          </Card>
        </Col>
        <Col xs={24} md={8}>
          <Card title="Completed Orders" className="stat-card">
            <Title level={4}>{completedOrders}</Title>
          </Card>
        </Col>
      </Row>
      <Row style={{ marginTop: "20px" }}>
        <Col span={24}>
          <Card title="Order Trend Over Time" className="chart-card">
            <ResponsiveContainer width="100%" height={400}>
              <ComposedChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis
                  dataKey="date"
                  tickFormatter={formatXAxis}
                  interval="preserveStartEnd"
                  minTickGap={30}
                />
                <YAxis 
                  allowDecimals={false}
                  domain={[0, 'dataMax + 1']}
                  tickCount={5}
                />
                <Tooltip
                  labelFormatter={(label) => dayjs(label).format("YYYY-MM-DD")}
                  formatter={(value, name) => [`${value} Orders`, name]}
                />
                <Legend />
                <Bar
                  dataKey="CANCELLED"
                  fill="#ff4d4f"
                  name="Cancelled Orders"
                  stackId="a"
                />
                <Bar
                  dataKey="COMPLETED"
                  fill="#52c41a"
                  name="Completed Orders"
                  stackId="a"
                />
              </ComposedChart>
            </ResponsiveContainer>
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default OrderChart;